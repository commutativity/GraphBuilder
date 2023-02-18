package utils

import analysis.StartConversion
import org.apache.spark.graphx.Graph
import org.apache.spark.sql.{DataFrame, Row, functions}
import org.graphframes.GraphFrame
import utils.Spark._
import utils.flattenDataframe._

import java.io.PrintWriter

object GEXFConverter {

  import sparkSession.implicits._


  /**
   * @param queryDataFrame the DataFrame from motif search or shortest paths
   * @param file_location the location for the resulting GEXF
   * @param gexfName the file name of the resulting GEXF
   */
  def convertResults(queryDataFrame: DataFrame, file_location: String, gexfName: String): String = {
    val flattenedDataFrame = queryDataFrame.select(flattenStructSchema(queryDataFrame.schema): _*)

    // run the node extraction on the GraphFrame motif query
    val graph = toVertexEdgeDataFrames(flattenedDataFrame)

    val gexfString = writeGEXF(graph, file_location, gexfName)

    // start the preview
    StartConversion.run(gexfName)

    gexfString
  }


  /**
   * @param inputDataFrame DataFrame with multiple node and edge columns
   */
  def toVertexEdgeDataFrames(inputDataFrame: DataFrame): GraphFrame = {
    // get the column names and separate column names for vertices and edge tuples (with src and dst)
    val colNames = inputDataFrame.schema.fieldNames
    val vertex_columns = colNames.filter(elem => elem.contains("id") || elem.contains("redirectTitle")
      || elem.contains("category"))
    val edge_columns = colNames.filter(elem => elem.contains("src") || elem.contains("dst"))

    // save vertices and edges dataframe
    val selected_vertex_columns = vertex_columns.map(name => inputDataFrame.col(name))
    var vertex_DF = inputDataFrame.select(selected_vertex_columns: _*)
    val selected_edges_columns = edge_columns.map(name => inputDataFrame.col(name))
    var edge_DF = inputDataFrame.select(selected_edges_columns: _*)

    // combine vertices in a dataframe with flatMap
    vertex_DF = vertex_DF.flatMap(row => {
      val vertex_columns_grouped = vertex_columns.grouped(3)
      for (name <- vertex_columns_grouped) yield {
        (row.getAs[String](name(0)), row.getAs[String](name(1)), row.getAs[String](name(2)))
      }
    }).toDF("id", "redirectTitle", "category").distinct()

    // combine edge tuples (src and dst) in a dataframe flatMap
    edge_DF = edge_DF.flatMap(row => {
      val edge_columns_grouped = edge_columns.grouped(2)
      for (name <- edge_columns_grouped) yield {
        (row.getAs[String](name(0)), row.getAs[String](name(1)))
      }
    }).toDF("src", "dst").distinct()

    // replace characters that lead to errors during the graph layout and in Gephi
    vertex_DF = vertex_DF.withColumn("id", functions
      .regexp_replace(vertex_DF.col("id"), "&", "and"))
    vertex_DF = vertex_DF.withColumn("id", functions
      .regexp_replace(vertex_DF.col("id"), "\"", "'"))
    vertex_DF = vertex_DF.withColumn("redirectTitle", functions
      .regexp_replace(vertex_DF.col("redirectTitle"), "\"", "'"))

    edge_DF = edge_DF.withColumn("src", functions
      .regexp_replace(edge_DF.col("src"), "&", "and"))
    edge_DF = edge_DF.withColumn("dst", functions
      .regexp_replace(edge_DF.col("dst"), "&", "and"))
    edge_DF = edge_DF.withColumn("src", functions
      .regexp_replace(edge_DF.col("src"), "\"", "'"))
    edge_DF = edge_DF.withColumn("dst", functions
      .regexp_replace(edge_DF.col("dst"), "\"", "'"))


    GraphFrame(vertex_DF, edge_DF)
  }


  /**
   * @param g GraphX object which is converted to GEXF
   * @tparam VD Vertices DataFrame
   * @tparam ED Edges DataFrame
   * @return A string which has the format of a GEXF file
   */
  def toGexf[VD, ED](g: Graph[VD, ED]): String = {
    val header =
      """<?xml version="1.0" encoding="UTF-8"?>
        |<gexf xmlns="https://www.gexf.net/1.2draft" version="1.2">
        |<meta>
        |<description>A gephi graph in GEXF format</description>
        |</meta>
        |<graph mode="static" defaultedgetype="directed">
        |<attributes class="node">
        |<attribute id="1" title="redirect" type="string"/>
        |<attribute id="2" title="namespace" type="string"/>
        |<attribute id="3" title="category" type="string"/>
        |</attributes>
      """.stripMargin


    val vertices = "<nodes>\n" + g.vertices.map(
      v => s"""<node id=\"${v._1}\" label=\"${v._2.asInstanceOf[Row].getAs("id")}\">\n
      <attvalues>\n
      <attvalue id="1" value=\"${v._2.asInstanceOf[Row].getAs("redirectTitle")}\"/>
      <attvalue id="2" value="0"/>
      <attvalue id="3" value=\"${v._2.asInstanceOf[Row].getAs("category")}\"/>
      </attvalues>\n
      </node>"""
    ).collect.mkString + "</nodes>\n"

    val edges = "<edges>\n" + g.edges.map(
      e =>
        s"""<edge source=\"${e.srcId}\" target=\"${e.dstId}\"
  label=\"${e.attr}\"/>\n"""
    ).collect.mkString + "</edges>\n"

    val footer = "</graph>\n</gexf>"

    header + vertices + edges + footer
  }


  /**
   * @param graph prepared GraphFrame object
   * @param fileLocation file location for the output GEXF
   * @param gexfName name of the output file
   */
  def writeGEXF(graph: GraphFrame, fileLocation: String, gexfName: String): String = {
    val subgraphX = graph.toGraphX
    val pw = new PrintWriter(fileLocation + gexfName + ".gexf")
    println("Saving: %s%s.gexf".format(fileLocation, gexfName))
    val gexfString = toGexf(subgraphX)
    pw.write(gexfString)
    pw.close()

    gexfString
  }
}

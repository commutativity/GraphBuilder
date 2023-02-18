package buildGraph

import org.apache.spark.sql.DataFrame
import utils.Spark._
import utils.timeFunctions._


object JoinPageRank extends App {

  run("allData")

  def run(nameGraph: String): Seq[String] = {

    val (block: String, time) = walltime {
      // access the vertices DataFrames
      var vertices_temp: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR_temp".format(nameGraph))
      val verticesCC: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC".format(nameGraph))

      // prepare the temp DataFrame, select required columns and rename
      vertices_temp = vertices_temp.select("id", "pagerank")
      vertices_temp = vertices_temp.withColumnRenamed("id", "id_temp")

      // perform the join and drop unnecessary columns
      var vertices = verticesCC.join(vertices_temp,
        verticesCC("id") === vertices_temp("id_temp"), "left")
      vertices = vertices.drop("id_temp")

      // save vertices
      vertices.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(nameGraph))


      // access the DataFrames
      var edges_temp: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR_temp".format(nameGraph))
      val edges: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/Edges".format(nameGraph))

      // prepare edges_temp three columns src, dst, weight
      edges_temp = edges_temp.drop("internalClickstream")
      edges_temp = edges_temp
        .withColumnRenamed("src", "src_temp")
        .withColumnRenamed("dst", "dst_temp")

      // perform the join and drop unnecessary columns
      var edgesPR = edges.join(edges_temp,
        edges("src") === edges_temp("src_temp") &&
        edges("dst") === edges_temp("dst_temp"), "left")
      edgesPR = edgesPR.drop("src_temp", "dst_temp")

      // save edges
      edgesPR.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR".format(nameGraph))

      // prepare runtime statistics
      var graphEntities = vertices.count().toString
      graphEntities += " vertices, "
      graphEntities += edgesPR.count().toString
      graphEntities += " edges"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    println(scriptLogs)
    scriptLogs
  }
}

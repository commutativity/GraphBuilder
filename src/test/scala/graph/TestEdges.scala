package graph

import org.apache.spark.sql.DataFrame
import org.graphframes.GraphFrame
import utils.Spark.{sparkSession, sqlContext}

object TestEdges extends App {

  import sparkSession.implicits._

  val graphModel = "allData"
  var Vertices: DataFrame = sqlContext.read.parquet(("hdfs://" +
    "0.0.0.0:19000/user/esse/%s/VerticesCC_PR").format(graphModel))
  var Edges: DataFrame = sqlContext.read.parquet(("hdfs://" +
    "0.0.0.0:19000/user/esse/%s/Edges").format(graphModel))

  // filter out null values
  Vertices = Vertices.filter($"id".isNotNull)
  Vertices = Vertices.filter("namespace == 0")
  Edges = Edges.filter($"src".isNotNull)

  // create GraphFrame and get stats
  val g: GraphFrame = GraphFrame(Vertices, Edges)

  val test = g.find("(n1)-[e]->(n2)")
  val t1 = test.filter("n2.id = 'Robert Frost'").distinct()
  println(t1.count())

}

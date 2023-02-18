package buildGraph

import org.apache.spark.sql.DataFrame
import org.graphframes.GraphFrame
import utils.Spark.{sparkSession, sqlContext}
import utils.timeFunctions.{logParser, walltime}


object PageRankAlgo extends App {

  run("allData")
  import sparkSession.implicits._

  def run(graphModel: String): Seq[String] = {

    val (block: String, time) = walltime {
      var vertices: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC".format(graphModel))
      var edges: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/Edges".format(graphModel))

      // remove the redirect from vertex DataFrame
      vertices = vertices.filter($"redirectTitle".isNull).filter("namespace = 0")
      println(vertices.count())
      vertices = vertices.filter($"category".isNotNull)
      println(vertices.count())

      // run the PageRank algo
      val g: GraphFrame = GraphFrame(vertices, edges)
      val result = g.pageRank.resetProbability(0.15).maxIter(10).run()

      result.vertices.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graphModel))
      result.edges.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR".format(graphModel))

      var graphEntities = result.vertices.count().toString
      graphEntities += " vertices, "
      graphEntities += result.edges.count().toString
      graphEntities += " edges"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

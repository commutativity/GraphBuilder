package buildGraph

import org.apache.spark.sql.DataFrame
import org.graphframes.GraphFrame
import utils.Spark._
import utils.timeFunctions.{logParser, walltime}


object CCAlgo extends App {

  run("wiki_analysis")

  def run(nameGraph: String): Seq[String] = {

    val (block: String, time) = walltime {
      // set checkpoint directory
      sparkSession.sparkContext.setCheckpointDir("/tmp/checkpoints")

      // load the source DataFrames
      val Vertices: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/Vertices".format(nameGraph))
      val Edges: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/Edges".format(nameGraph))

      // run the connected components algo and save the results
      val g: GraphFrame = GraphFrame(Vertices, Edges)
      val result = g.connectedComponents.run()

      result.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC".format(nameGraph))

      var graphEntities = result.count().toString
      graphEntities += " vertices"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

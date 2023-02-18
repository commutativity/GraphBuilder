package buildGraph

import org.apache.spark.sql.DataFrame
import utils.timeFunctions._
import utils.Spark._


object JoinEdges extends App {

  run("modelThree")

  def run(nameGraph: String): Seq[String] = {
    val (block: String, time) = walltime {
      val EdgesR_DF: DataFrame = sqlContext
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesR".format(nameGraph))
      val EdgesCS_DF: DataFrame = sqlContext
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesCS".format(nameGraph))


      var Edges = EdgesR_DF.join(EdgesCS_DF, EdgesR_DF("src") === EdgesCS_DF("prevTitle") &&
        EdgesR_DF("dst") === EdgesCS_DF("currTitle"), "left")
      Edges = Edges.drop("clickType", "prevTitle", "currTitle")

      Edges = Edges.dropDuplicates()

      Edges.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/Edges".format(nameGraph))

      var graphEntities = Edges.count().toString
      graphEntities += " edges"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

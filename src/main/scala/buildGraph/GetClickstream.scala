package buildGraph

import utils.timeFunctions._
import utils.Spark._
import org.apache.spark.sql.functions


object GetClickstream extends App {

  run("clickstream-enwiki-2022-12.tsv", "modelThree")

  def run(fileName: String, nameGraph: String): Seq[String] = {

    import sparkSession.implicits._

    val path = "src/main/resources/clickstream/" + fileName

    val (block: String, time) = walltime {
      var clickstreamDF = Parser.readClickSteam(sparkContext, path).toDF()

      // remove underscore and replace with space in edge clickstream DataFrame
      clickstreamDF = clickstreamDF.withColumn("prevTitle",
        functions.regexp_replace(clickstreamDF.col("prevTitle"), "_", " "))
      clickstreamDF = clickstreamDF.withColumn("currTitle",
        functions.regexp_replace(clickstreamDF.col("currTitle"), "_", " "))


      // separate external clickstream data
      val clickstreamDFEdge = clickstreamDF
        .filter(clickstreamDF("clickType") === "external")
        .drop("clickType")
        .withColumnRenamed("n", "clickstream")

      // save the edge results
      clickstreamDFEdge.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCS".format(nameGraph))


      // separate internal clickstream data
      val clickstreamDFLink = clickstreamDF
        .filter(clickstreamDF("clickType") === "link")
        .drop("clickType")
        .withColumnRenamed("n", "internalClickstream")

      clickstreamDFLink.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesCS".format(nameGraph))

      var graphEntities = clickstreamDFEdge.count().toString
      graphEntities += " vertices, "
      graphEntities += clickstreamDFLink.count().toString
      graphEntities += " edges"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

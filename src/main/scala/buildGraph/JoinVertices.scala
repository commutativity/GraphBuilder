package buildGraph

import org.apache.spark.sql.DataFrame
import utils.Spark._
import utils.timeFunctions._


object JoinVertices extends App {

  run("modelFour")

  def run(nameGraph: String): Seq[String] = {

    val (block: String, time) = walltime {

      val VerticesR_category: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesR_category".format(nameGraph))
      var VerticesCS: DataFrame = sparkSession
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCS".format(nameGraph))

      // aggregate values (other-external, other-search, other-internal, other-empty)
      VerticesCS = VerticesCS.select("currTitle", "clickstream").groupBy("currTitle").sum()

      // perform the join and drop unnecessary columns
      var vertices = VerticesR_category.join(VerticesCS,
        VerticesR_category("id") === VerticesCS("currTitle"), "left")
      vertices = vertices.drop("currTitle")
      vertices = vertices.withColumnRenamed("sum(clickstream)", "clickstream")

      // Save vertices
      vertices.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/Vertices".format(nameGraph))

      var graphEntities = vertices.count().toString
      graphEntities += " vertices"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

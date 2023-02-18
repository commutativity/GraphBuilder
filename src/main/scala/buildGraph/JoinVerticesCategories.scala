package buildGraph

import org.apache.spark.sql.functions.element_at
import org.apache.spark.sql.{DataFrame, functions}
import utils.timeFunctions._
import utils.Spark._


object JoinVerticesCategories extends App {

  run("wiki_analysis")

  def run(nameGraph: String): Seq[String] = {

    import sparkSession.implicits._

    val (block: String, time) = walltime {
      val categories_map: DataFrame = sqlContext
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/MapCategories".format(nameGraph))
      var VerticesR: DataFrame = sqlContext
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesR".format(nameGraph))

      // extract the second tag and replace spaces with underscores
      VerticesR = VerticesR.withColumn("tag", element_at($"tags", 2))
      VerticesR = VerticesR.withColumn("tag",
        functions.regexp_replace(VerticesR.col("tag"), "\\s", "_"))

      // perform the match with categories
      val VerticesR_category = VerticesR
        .join(categories_map, VerticesR("tag") === categories_map("subCategory"), "left")
        .drop("subCategory")

      VerticesR_category.write.mode("overwrite")
        .parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesR_category".format(nameGraph))

      var graphEntities = VerticesR.count().toString
      graphEntities += " vertices"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

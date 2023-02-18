package buildGraph

import org.apache.spark.sql.functions.lit
import utils.timeFunctions._
import utils.Spark._

object MapCategories extends App {

  run("modelOne")

  def run(nameGraph: String): Seq[String] = {
    val (block: String, time) = walltime {
      var df_cult = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/culture_depth4.csv")
      var df_geog = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/geography_depth4.csv")
      var df_heal = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/health_depth4.csv")
      var df_hist = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/history_depth4.csv")
      var df_huma = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/human_activities_depth4.csv")
      var df_math = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/mathematics_depth4.csv")
      var df_natu = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/nature_depth4.csv")
      var df_peop = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/people_depth4.csv")
      var df_phil = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/philosophy_depth4.csv")
      var df_reli = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/religion_depth4.csv")
      var df_soci = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/society_depth4.csv")
      var df_tech = sparkSession.read.option("header", value = true).csv("src/main/resources/categories/technology_depth4.csv")

      df_cult = df_cult.withColumn("category", lit("culture"))
      df_geog = df_geog.withColumn("category", lit("geography"))
      df_heal = df_heal.withColumn("category", lit("health"))
      df_hist = df_hist.withColumn("category", lit("history"))
      df_huma = df_huma.withColumn("category", lit("human_activities"))
      df_math = df_math.withColumn("category", lit("mathematics"))
      df_natu = df_natu.withColumn("category", lit("nature"))
      df_peop = df_peop.withColumn("category", lit("people"))
      df_phil = df_phil.withColumn("category", lit("philosophy"))
      df_reli = df_reli.withColumn("category", lit("religion"))
      df_soci = df_soci.withColumn("category", lit("society"))
      df_tech = df_tech.withColumn("category", lit("technology"))

      var df = df_cult.union(df_geog).union(df_heal).union(df_hist).union(df_huma)
        .union(df_math).union(df_natu).union(df_peop).union(df_phil).union(df_reli).union(df_soci).union(df_tech)
      df = df.dropDuplicates("title")

      df = df.drop("number", "namespace", "touched", "pageid", "length")
        .withColumnRenamed("title", "subCategory")
      df.write.mode("overwrite").parquet("hdfs://0.0.0.0:19000/user/esse/%s/MapCategories".format(nameGraph))

      var mapEntities = df.count().toString
      mapEntities += " subcategory-category pairs"
      mapEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

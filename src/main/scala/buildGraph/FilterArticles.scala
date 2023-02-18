package buildGraph

import org.apache.spark.sql.functions._
import utils.timeFunctions._
import utils.flattenDataframe
import utils.Spark._

object FilterArticles extends App {

  run("enwiki-20221101-multistream1.xml-p1p41242",
    "modelThree")
  import sparkSession.implicits._

  def run(fileName: String, nameGraph: String): Seq[String] = {

    val path = "src/main/resources/articles/" + fileName

    val (block: String, time) = walltime {
      val raw_pages = Parser.readWikiDump(sparkContext, path)

      // convert RDD to DataFrame and get namespace
      var raw_pagesDF = raw_pages.toDF("id", "text")
      val convertCaseArticle = (str: String) => {
        val bracketMatcher = """<ns>(.)</ns>""".r
        val matches = bracketMatcher.findFirstMatchIn(str)
        matches.map(_.group(1)).mkString // extracts the namespace
      }
      val findArticle = udf(convertCaseArticle)
      raw_pagesDF = raw_pagesDF.withColumn("namespace", findArticle(col("text")))


      // get categories
      val convertCase = (str: String) => {
        val bracketMatcher = """\[\[Category:(.*?)\]\]""".r
        val matches = bracketMatcher.findAllMatchIn(str)
        matches.map(_.group(1)).mkString(",")
      }
      val get_categories = udf(convertCase)
      raw_pagesDF = raw_pagesDF
        .withColumn("tags", get_categories(col("text")))
        .withColumn("tags", split(col("tags"), ","))
        .drop("text") // raw_pagesDF columns: id (false), namespace, tags


      // get title
      val pages = Parser.parsePages(raw_pages)
      var pagesDF = pages.toDF("id", "title")
      //      pagesDF.show(20, 200)
      pagesDF = pagesDF.select(flattenDataframe.flattenSchema(pagesDF.schema): _*) // pagesDF columns: id (false), title, text
      //      pagesDF.show(20, 200)                                                            // note: the title contains spaces

      // get redirects and join with title
      val redirectsDF = Parser.parseRedirects(pages.values).toDF() // pageTitle (could be with space), redirectTitle (with space)
      pagesDF = pagesDF.join(redirectsDF, pagesDF("title") === redirectsDF("pageTitle"), "outer") // pagesDF: id, title, text, pageTitle, redirectTitle


      // join title, namespace, redirects in VerticesR
      pagesDF = pagesDF.join(raw_pagesDF, pagesDF("id") === raw_pagesDF("id"), "outer") // id, title, text, pageTitle, redirectTitle, namespace, tags
      pagesDF = pagesDF.drop("id", "text", "pageTitle") // title, redirectTitle, namespace, tags
      // rename the first column
      pagesDF = pagesDF.withColumnRenamed("title", "id") // title -> id, redirectTitle, namespace, tags


      // save VerticesR
      pagesDF.write.mode("overwrite").parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesR".format(nameGraph))


      // get internal links
      var linksDF = Parser.parseInternalLinks(pages.values).toDF()
      linksDF = linksDF.drop("row", "col")
      linksDF = linksDF
        .withColumnRenamed("pageTitle", "src")
        .withColumnRenamed("linkTitle", "dst")
      linksDF = linksDF.filter("dst != ''") // filter out empty strings


      // save EdgesR
      linksDF.write.mode("overwrite").parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesR".format(nameGraph))

      var graphEntities = pagesDF.count().toString
      graphEntities += " vertices, "
      graphEntities += linksDF.count().toString
      graphEntities += " edges"
      graphEntities
    }

    // return the script logs
    val scriptLogs: Seq[String] = logParser(time, block)
    scriptLogs
  }
}

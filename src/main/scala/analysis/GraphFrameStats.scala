package analysis

import java.nio.file.{Files, Paths}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.DataFrame
import org.graphframes.GraphFrame
import utils.Spark._
import utils.timeFunctions._

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source


object GraphFrameStats extends App {

  import sparkSession.implicits._

  run(graphModel = "allData")

  case class vertexInDeg(average_in: String)
  case class vertexOutDeg(average_out: String)
  case class VN (namespace: String, count: String, type_namespace: String)
  case class VC (category: String, count: String)
  case class VCS (id: String, clickstream: String)
  case class CO (component: String, count: String)
  case class PR (id: String, pagerank: String)
  case class PRM (pr: String)

  def run(graphModel: String): String = {

    val location = "src/main/resources/txt/"
    val fileEnding = ".txt"
    val path = location + graphModel + fileEnding
    val nioPath = Paths.get(path)
    var runLog = ""

    // if the file exists, print it and exit
    if (Files.exists(nioPath)) {
      val content = Source.fromFile(path)
      for (line <- content.getLines()) {
        runLog += line + "\n"
      }
    } else {

      // write the header of the file
      var text = "\t_________________________ \n"
      text +=    "\t| GraphFrame Statistics  |\n"
      text +=    "\t_________________________ \n"

      val timeString = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now())
      text += "\nResults are for graph: %s\n".format(graphModel)
      text += "File is created at: %s\n".format(timeString)

      // initialize json file
      val jsonFile = ujson.Obj()
      jsonFile("Graph") = ujson.Str(graphModel)
      jsonFile("CreationTimestamp") = ujson.Str(timeString)

      val (_, time) = walltime {
        var Vertices: DataFrame = sqlContext.read.parquet(("hdfs://" +
          "0.0.0.0:19000/user/esse/%s/VerticesCC_PR").format(graphModel))
        var Edges: DataFrame = sqlContext.read.parquet(("hdfs://" +
          "0.0.0.0:19000/user/esse/%s/Edges").format(graphModel))

        // filter out null values
        Vertices = Vertices.filter($"id".isNotNull)
        Edges = Edges.filter($"src".isNotNull)

        // create GraphFrame and get stats
        val g: GraphFrame = GraphFrame(Vertices, Edges)
        val numberVertices = g.vertices.count().toString
        val numberEdges = g.edges.count().toString
        text += "\n\nGraph size -------------------------------\n"
        text += "Number of vertices: %s\n".format(numberVertices)
        text += "Number of edges: %s\n".format(numberEdges)
        jsonFile("NumberVertices") = ujson.Str(numberVertices)
        jsonFile("NumberEdges") = ujson.Str(numberEdges)

        // in and out degrees take long to compute, confirm with true
        if (true) {
          text += "\n\nIn- and out degrees -------------------------------\n"
          var vertexInDegrees: DataFrame = g.inDegrees
          vertexInDegrees = vertexInDegrees.select(avg($"inDegree"))
            .withColumnRenamed("avg(inDegree)", "average_in")
          vertexInDegrees.as[vertexInDeg].take(vertexInDegrees.count().toInt)
            .foreach(a => {
              text += "AverageIncoming: %s\n".format(a.average_in)
              jsonFile("AverageIncoming") = ujson.Str(a.average_in)
            }
            )

          var vertexOutDegrees: DataFrame = g.outDegrees
          vertexOutDegrees = vertexOutDegrees.select(avg($"outDegree"))
            .withColumnRenamed("avg(outDegree)", "average_out")
          vertexOutDegrees.as[vertexOutDeg].collect()
            .foreach(a => {
              text += "AverageOutgoing: %s\n".format(a.average_out)
              jsonFile("AverageOutgoing") = ujson.Str(a.average_out)
            }
            )
        }


        text += "\n\nRedirects -------------------------------\n"
        val numberArticles = Vertices.filter($"redirectTitle".isNull).count().toString
        val numberRedirects = Vertices.filter($"redirectTitle".isNotNull).count().toString
        text += "Number  Article: %s\n".format(numberArticles)
        text += "Number Redirect: %s\n".format(numberRedirects)
        jsonFile("NumberArticles") = ujson.Str(numberArticles)
        jsonFile("NumberRedirects") = ujson.Str(numberRedirects)


        text += "\n\nNamespaces -------------------------------\n"
        var Vertices_Namespaces = Vertices.groupBy($"namespace").count()
        Vertices_Namespaces = Vertices_Namespaces.withColumn("type_namespace",
          when(col("namespace") === "0", lit("Article"))
            .when(col("namespace") === "4", lit("Wikipedia"))
            .when(col("namespace") === "6", lit("File"))
            .when(col("namespace") === "8", lit("MediaWiki"))
            .otherwise(lit("has-no-namespace")))
        Vertices_Namespaces.as[VN].collect().foreach(
          a => {
            text += "Namespace type %s occurs %s times.\n".format(a.type_namespace, a.count)
            jsonFile("Namespace%s".format(a.type_namespace)) = ujson.Str(a.count)
          }
        )

        text += "\n\nMain categories -------------------------------\n"
        val Vertices_Categories = Vertices.groupBy($"category").count()
        Vertices_Categories.as[VC].collect().foreach(
          a => {
            text += "Category %s occurs %s times.\n".format(a.category, a.count)
            jsonFile("MainCategory%s".format(a.category)) = ujson.Str(a.count)
          }
        )

        val numberCluster = g.vertices.select("component").distinct().count()
        text += "\n\nTotal number of ComponentIDs: %s\n".format(numberCluster)
        jsonFile("NumberCluster") = ujson.Str(numberCluster.toString)

        // for the following exist advanced getter methods, confirm with true
        if (true) {
          text += "\n\nClickstream Top 5  -------------------------------\n"
          val Vertices_Clickstream = Vertices.select("id", "clickstream").sort($"clickstream".desc)
          Vertices_Clickstream.as[VCS].take(5).foreach(
            a => text += "Article %s was clicked %d times.\n".format(a.id, a.clickstream.toInt)
          )
          val VerticesWithCSData = Vertices.filter($"clickstream".isNotNull).count().toString
          text += "VerticesWithCSData: %s\n".format(VerticesWithCSData)
          jsonFile("VerticesWithCSData") = ujson.Str(VerticesWithCSData)
          val EdgesWithCSData = Edges.select($"internalClickstream".isNotNull).count().toString
          text += "EdgesWithCSData: %s\n".format(EdgesWithCSData)
          jsonFile("EdgesWithCSData") = ujson.Str(EdgesWithCSData)

          text += "\n\nTop 5 largest ComponentIDs and their node member numbers -------------------------------\n"
          val Components = g.vertices.select("component").groupBy("component").count().sort($"count".desc)
          Components.as[CO].take(5).foreach(
            a => text += "ComponentID %s occurred %d times.\n".format(a.component, a.count.toInt)
          )

          text += "\n\nGlobal Top 5 PageRank scores -------------------------------\n"
          val PageRankScores = g.vertices.sort($"pagerank".desc)
          PageRankScores.as[PR].take(5).foreach(
            a => text += "Article %s had a global PageRank score of %s.\n".format(a.id, a.pagerank)
          )
          Vertices.select(avg("pagerank")).withColumnRenamed("avg(pagerank)", "pr").as[PRM].take(1).foreach(
            a => text += "PageRankMean: %s\n".format(a.pr)
          )
        }
      }

      text += "\n\nRuntime to compute the results (walltime): %s seconds".format(time/1000000000)

      // create txt file and write contents to it
      try {
        Files.write(nioPath, text.getBytes(StandardCharsets.UTF_8))
        println("Writing to file successful")
      } catch {
        case e: IOException =>
          println(e)
      }

      // write contents to json file
      os.write(os.pwd/"src"/"main"/"resources"
        /"json"/"%s.json".format(graphModel), jsonFile)

      runLog = text
    }  // end if construct

    // return runLog
    runLog
  }  // end run method
}

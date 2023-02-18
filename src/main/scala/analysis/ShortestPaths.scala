package analysis

import utils.Analysis.getDataFrame
import utils.Spark._
import utils.timeFunctions.walltime
import utils.GEXFConverter._


object ShortestPaths extends App {

  run("modelOne", "Berlin", "Stuttgart", "BerlinPath")


  def run(graphName: String, start_node: String, end_node: String, gexfName: String): String = {

    val fileLocation = "D:\\scala_wiki\\sbt-test1\\src\\main\\resources\\gexf\\"

    val (_, time) = walltime {

      val graphModel = getDataFrame(graphName)
      println("GraphFrame build. Searching now.")

      val paths = graphModel.bfs.fromExpr("id = '%s'".format(start_node))
        .toExpr("id = '%s'".format(end_node)).maxPathLength(4).run()

      convertResults(paths, fileLocation, gexfName)
    }

    val runtime = "%s seconds".format(time / 1000000000)
    runtime
  }
}
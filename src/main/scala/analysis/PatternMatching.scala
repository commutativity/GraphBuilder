package analysis

import utils.Analysis.getDataFrame
import utils.timeFunctions._
import utils.GEXFConverter._


object PatternMatching extends App {

  run("modelOne",
    "node1",
    "edge1",
    "node2",
    "",
    "",
    "node1",
    "id",
    "=",
    "Software",
    "",
    "",
    "",
    "",
    "Software")

  def run(graphName: String,
          node1: String,
          edge1: String = "edge1",
          node2: String = "node2",
          edge2: String,
          node3: String,
          key: String,
          property: String,
          option: String,
          value: String,
          key1: String,
          property1: String,
          option1: String,
          value1: String,
          gexfName: String): String = {

    //    val query1 = "(%s)-[%s]->(%s)".format(node1, edge1, node2)
    val query2 = "(%s)-[%s]->(%s); (%s)-[%s]->(%s)".format(node1, edge1, node2, node2, edge2, node3)
    val fileLocation = "src\\main\\resources\\gexf\\"


    val (_, time) = walltime {
      val graphModel = getDataFrame(graphName)

      var neighbors = graphModel.find("%s".format(query2))
      neighbors = neighbors.filter("%s.%s %s '%s'".format(key, property, option, value))

      // the advanced filter functionality
      if (key1 != "") {
        neighbors = neighbors.filter("%s.%s %s '%s'".format(key1, property1, option1, value1))
      }

      val gexfString = convertResults(neighbors, fileLocation, gexfName)
    }

    val runtime = "%s seconds".format(time / 1000000000)
    runtime
  }
}
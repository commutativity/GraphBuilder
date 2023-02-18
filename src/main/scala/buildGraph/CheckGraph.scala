package buildGraph

import org.apache.spark.sql.DataFrame
import utils.Spark._


object CheckGraph extends App {

  run("modelThree")

  def run(graphModel: String): String = {

    val vertices: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graphModel))
    val edges: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR".format(graphModel))

    var graphEntities = vertices.count().toString
    graphEntities += " vertices, "
    graphEntities += edges.count().toString
    graphEntities += " edges"
    graphEntities
  }
}

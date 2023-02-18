package utils

import org.apache.spark.sql.DataFrame
import org.graphframes.GraphFrame
import utils.Spark._

object Analysis {

  def getDataFrame(graph: String): GraphFrame = {
    var verticesDF: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graph))
    val edgesDF: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR".format(graph))

    verticesDF = verticesDF.drop("text")
    GraphFrame(verticesDF, edgesDF)
  }

}

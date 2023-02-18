package analysis

import javafx.scene.{chart => javaFX}
import org.apache.spark.sql.functions.{col, monotonically_increasing_id}
import org.apache.spark.sql._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.XYChart
import utils.Spark._
import utils.timeFunctions.walltime

object GetProperty {

  import sparkSession.implicits._


  def getDataFrameColumns(graphModel: String): Array[String] = {
    val vertices: DataFrame = sqlContext
      .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graphModel))

    val list = vertices.columns
    list
  }


  def createChartData(graphModel: String,
                      textProperty: String,
                      numberEntries: Integer):
  ObservableBuffer[javaFX.XYChart.Series[String, Number]] = {

    var vertices: DataFrame = sqlContext
      .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graphModel))

    // decide on number or category data extraction with grouping
    if (vertices.schema(textProperty).dataType.toString == "StringType") {
      vertices = getDataCategory(vertices, textProperty)
    } else {
      vertices = getDataNumber(vertices, textProperty)
    }

    val answer = new ObservableBuffer[javaFX.XYChart.Series[String, Number]]()
    val cs = new XYChart.Series[String, Number] {
      name = textProperty
    }
    for (i <- 1 to numberEntries) {
      val one = vertices.take(i).last.get(0)
      val two = vertices.take(i).last.get(1)
      cs.data() += XYChart.Data[String, Number](one.toString, two.asInstanceOf[Number])
    }
    answer.add(cs)
    answer
  }


  def getDataNumber(Vertices: DataFrame, requestedProperty: String): DataFrame = {
    val VerticesPrep = Vertices.select("id", "%s".format(requestedProperty))
      .sort($"$requestedProperty".desc)
    VerticesPrep
  }

  def getDataCategory(Vertices: DataFrame, requestedProperty: String): DataFrame = {
    var VerticesPrep = Vertices.select(requestedProperty).groupBy(requestedProperty).count()
      .sort($"count".desc)

    VerticesPrep = VerticesPrep.na.fill("no value")

    VerticesPrep.show()
    VerticesPrep
  }



  def exportCSChartData(graph: String, folderName: String, downloadAll: Boolean,
                        fileLocation: String, displayedEntries: Int,
                        outputFormat: String, property: String): String = {

    val (_, time) = walltime {

      var vertices: DataFrame = sqlContext
        .read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graph))
      vertices = vertices.drop("tags")

      vertices = vertices.sort($"$property".desc).withColumn("RowID", monotonically_increasing_id())

      if (!downloadAll) {
        val RequiredIDs = List.range(1, displayedEntries)
        vertices = vertices.filter(col("RowID").isin(RequiredIDs: _*))
      }

      vertices.write.format(outputFormat).option("header", value = true).save(fileLocation + folderName)
    }

    val runtime = "%s seconds".format(time / 1000000000)
    runtime
  }
}

package analysis

import javafx.collections.{FXCollections, ObservableList}
import org.apache.spark.sql.DataFrame
import utils.Spark._

object GetDataFrame extends App {

  import sparkSession.implicits._
  case class EdgeDF (src: String, dst: String, internalClickstream: Option[Long], weight: Option[Double])

  case class VertexDF(id: String, redirectTitle: String, namespace: String,
                      tags: Array[String], category: String, clickstream: Option[Long],
                      component: Option[Long], pagerank: Option[Double])


  def runEdgeDataFrame(graphModel: String): ObservableList[EdgeClass] = {

    var edges: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/EdgesPR".format(graphModel))

    edges = edges.dropDuplicates()

    var srcItems: Seq[String] = Seq()
    var dstItems: Seq[String] = Seq()
    var internalClickstreamItems: Seq[Option[Long]] = Seq()
    var weightItems: Seq[Option[Double]] = Seq()

    edges.as[EdgeDF].take(500).foreach(a => {
      srcItems = srcItems :+ a.src
      dstItems = dstItems :+ a.dst
      internalClickstreamItems = internalClickstreamItems :+ a.internalClickstream
      weightItems = weightItems :+ a.weight
    })


    val list: ObservableList[EdgeClass] = FXCollections.observableArrayList()

    for (index <- 1 to 499) {
      list.add(new EdgeClass(
        srcItems.apply(index),
        dstItems.apply(index),
        internalClickstreamItems.apply(index),
        weightItems.apply(index)
      ))
    }

    list
  }



  def runVertexDataFrame(graphModel: String): ObservableList[VertexClass] = {

    var vertices: DataFrame = sqlContext.read.parquet("hdfs://0.0.0.0:19000/user/esse/%s/VerticesCC_PR".format(graphModel))

    vertices = vertices.dropDuplicates()

    var idItems: Seq[String] = Seq()
    var redirectTitleItems: Seq[String] = Seq()
    var namespaceItems: Seq[String] = Seq()
    var tagsItems: Seq[String] = Seq()
    var categoryItems: Seq[String] = Seq()
    var clickstreamItems: Seq[Option[Long]] = Seq()
    var componentItems: Seq[Option[Long]] = Seq()
    var pagerankItems: Seq[Option[Double]] = Seq()

    vertices.as[VertexDF].take(500).foreach(a => {
      idItems = idItems :+ a.id
      redirectTitleItems = redirectTitleItems :+ a.redirectTitle
      namespaceItems = namespaceItems :+ a.namespace
      tagsItems = tagsItems :+ a.tags.mkString("Array(", ", ", ")")
      categoryItems = categoryItems :+ a.category
      clickstreamItems = clickstreamItems :+ a.clickstream
      componentItems = componentItems :+ a.component
      pagerankItems = pagerankItems :+ a.pagerank
    })


    val list: ObservableList[VertexClass] = FXCollections.observableArrayList()

    for (index <- 1 to 499) {
      list.add(new VertexClass(
        idItems.apply(index),
        redirectTitleItems.apply(index),
        namespaceItems.apply(index),
        tagsItems.apply(index),
        categoryItems.apply(index),
        clickstreamItems.apply(index),
        componentItems.apply(index),
        pagerankItems.apply(index)
      ))
    }

    list
  }

}

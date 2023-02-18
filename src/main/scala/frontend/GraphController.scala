package frontend

import analysis.{EdgeClass, GetDataFrame, VertexClass}
import buildGraph.{CCAlgo, CheckGraph, FilterArticles, GetClickstream, JoinEdges, JoinVertices, JoinVerticesCategories, MapCategories, PageRankAlgo}
import javafx.collections.ObservableList
import javafx.fxml._
import javafx.scene.control._
import javafx.event._
import javafx.scene.control.cell.PropertyValueFactory
import utils.timeFunctions.walltime

import java.awt.Desktop
import java.net.URL
import java.util.ResourceBundle

class GraphController extends Initializable {

  @FXML private var nameModel: TextField = _
  @FXML private var log1: Label = _
  @FXML private var nameArticleDataset: TextField = _
  @FXML private var log2: Label = _
  @FXML private var nameClickstreamDataset: TextField = _
  @FXML private var log3: Label = _
  @FXML private var log4: Label = _
  @FXML private var log5: Label = _
  @FXML private var log6: Label = _
  @FXML private var log7: Label = _
  @FXML private var log8: Label = _
  @FXML private var log9: Label = _

  // edge table variable declarations
  @FXML private var srcColumn: TableColumn[EdgeClass, String] = _
  @FXML private var dstColumn: TableColumn[EdgeClass, String] = _
  @FXML private var internalClickstreamColumn: TableColumn[EdgeClass, Option[Long]] = _
  @FXML private var weightColumn: TableColumn[EdgeClass, Double] = _
  @FXML private var edgeTable: TableView[EdgeClass] = _

  // vertex table variable declarations
  @FXML private var vertexTable: TableView[VertexClass] = _
  @FXML private var idColumn: TableColumn[VertexClass, String] = _
  @FXML private var redirectTitleColumn: TableColumn[VertexClass, String] = _
  @FXML private var namespaceColumn: TableColumn[VertexClass, String] = _
  @FXML private var tagsColumn: TableColumn[VertexClass, String] = _
  @FXML private var categoryColumn: TableColumn[VertexClass, String] = _
  @FXML private var clickstreamColumn: TableColumn[VertexClass, Option[Long]] = _
  @FXML private var componentColumn: TableColumn[VertexClass, Option[Long]] = _
  @FXML private var pagerankColumn: TableColumn[VertexClass, Double] = _

  // graph items
  @FXML private var items1: Label = _
  @FXML private var items2: Label = _
  @FXML private var items3: Label = _
  @FXML private var items4: Label = _
  @FXML private var items5: Label = _
  @FXML private var items6: Label = _
  @FXML private var items7: Label = _
  @FXML private var items8: Label = _
  @FXML private var items9: Label = _


  @FXML private def downloadCategory(event: ActionEvent): Unit = {
    val desktop = Desktop.getDesktop
    desktop.browse(java.net.URI.create("https://petscan.wmflabs.org"))
  }

  @FXML private def downloadArticle(event: ActionEvent): Unit = {
    val desktop = Desktop.getDesktop
    desktop.browse(java.net.URI.create("https://dumps.wikimedia.org/backup-index.html"))
  }

  @FXML private def downloadClickstream(event: ActionEvent): Unit = {
    val desktop = Desktop.getDesktop
    desktop.browse(java.net.URI.create("https://dumps.wikimedia.org/other/clickstream/"))
  }

  @FXML private def runMapCategories(event: ActionEvent): Unit = {
    val scriptLogs = MapCategories.run(nameModel.getText)
    log1.setText(scriptLogs.head)
    items1.setText(scriptLogs.apply(1))
  }

  @FXML private def runFilterArticles(event: ActionEvent): Unit = {
    val scriptLogs = FilterArticles.run(fileName = nameArticleDataset.getText,
      nameModel.getText)
    log2.setText(scriptLogs.head)
    items2.setText(scriptLogs.apply(1))
  }

  @FXML private def runGetClickstream(event: ActionEvent): Unit = {
    val scriptLogs = GetClickstream.run(nameClickstreamDataset.getText,
      nameModel.getText)
    log3.setText(scriptLogs.head)
    items3.setText(scriptLogs.apply(1))
  }

  @FXML private def runJoinVerticesCategories(event: ActionEvent): Unit = {
    val scriptLogs = JoinVerticesCategories.run(nameModel.getText)
    log4.setText(scriptLogs.head)
    items4.setText(scriptLogs.apply(1))
  }

  @FXML private def runJoinVertices(event: ActionEvent): Unit = {
    val scriptLogs = JoinVertices.run(nameModel.getText)
    log5.setText(scriptLogs.head)
    items5.setText(scriptLogs.apply(1))
  }

  @FXML private def runJoinEdges(event: ActionEvent): Unit = {
    val scriptLogs = JoinEdges.run(nameModel.getText)
    log6.setText(scriptLogs.head)
    items6.setText(scriptLogs.apply(1))
  }

  @FXML private def runCC(event: ActionEvent): Unit = {
    val scriptLogs = CCAlgo.run(nameModel.getText)
    log7.setText(scriptLogs.head)
    items7.setText(scriptLogs.apply(1))
  }

  @FXML private def runPR(event: ActionEvent): Unit = {
    val scriptLogs = PageRankAlgo.run(nameModel.getText)
    log8.setText(scriptLogs.head)
    items8.setText(scriptLogs.apply(1))
  }



  @FXML private def runCheck(event: ActionEvent): Unit = {

    val (_, time) = walltime {
      // update edge table
      val edgeList: ObservableList[EdgeClass] = GetDataFrame.runEdgeDataFrame(nameModel.getText)

      srcColumn.setCellValueFactory(new PropertyValueFactory[EdgeClass, String]("src"))
      dstColumn.setCellValueFactory(new PropertyValueFactory[EdgeClass, String]("dst"))
      internalClickstreamColumn.setCellValueFactory(
        new PropertyValueFactory[EdgeClass, Option[Long]]("internalClickstream"))
      weightColumn.setCellValueFactory(new PropertyValueFactory[EdgeClass, Double]("weight"))

      edgeTable.setItems(edgeList)


      // update vertex table
      val vertexList: ObservableList[VertexClass] = GetDataFrame.runVertexDataFrame(nameModel.getText)

      idColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, String]("id"))
      redirectTitleColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, String]("redirectTitle"))
      namespaceColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, String]("namespace"))
      tagsColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, String]("tags"))
      categoryColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, String]("category"))
      clickstreamColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, Option[Long]]("clickstream"))
      componentColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, Option[Long]]("component"))
      pagerankColumn.setCellValueFactory(new PropertyValueFactory[VertexClass, Double]("pagerank"))

      vertexTable.setItems(vertexList)


      // print graph items
      val graphEntities = CheckGraph.run(nameModel.getText)
      items9.setText(graphEntities)
    }

    // print the runtime
    val runtime = "%s seconds".format(time / 1000000000)
    log9.setText(runtime)
  }



  override def initialize(location: URL, resources: ResourceBundle): Unit = {
  }
}

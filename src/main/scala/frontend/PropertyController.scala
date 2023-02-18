package frontend

import javafx.fxml._
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.chart._

import java.net.URL
import java.util.ResourceBundle
import analysis.GetProperty.{createChartData, exportCSChartData, getDataFrameColumns}

class PropertyController extends Initializable {

  // variables
  @FXML private var barChart: BarChart[String, Number] = _
  @FXML private var graphModel: TextField = _
  @FXML private var textProperty: TextField = _
  @FXML private var textChartEntries: TextField = _
  @FXML private var folderName: TextField = _
  @FXML private var fileLocation: TextField = _
  @FXML private var outputFormat: TextField = _
  @FXML private var exportAll: CheckBox = _

  // methods
  @FXML private def updateEntries(event: ActionEvent): Unit = {
    // get columns
    val list = getDataFrameColumns(graphModel.getText)

    // check if columns is in available ones
    if (!list.contains(textProperty.getText)) {
      var alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Input error")
      alert.setContentText("Column is not valid.")
      alert.showAndWait()
    } else {
      // draw the chart
      barChart.setData(createChartData(
        graphModel.getText,
        textProperty.getText,
        textChartEntries.getText.toInt))
      barChart.setAnimated(false)
    }
  }

  @FXML private def exportEntries(event: ActionEvent): Unit = {
    exportCSChartData(graph = graphModel.getText, folderName = folderName.getText,
      downloadAll = exportAll.isSelected, fileLocation = fileLocation.getText,
      displayedEntries = textChartEntries.getText.toInt,
      outputFormat = outputFormat.getText, property = textProperty.getText)
  }


  override def initialize(location: URL, resources: ResourceBundle): Unit = {
  }
}

package frontend

import javafx.scene.{control => jfxsc}
import javafx.{event => jfxe, fxml => jfxf}

import java.net.URL
import java.util.ResourceBundle

import analysis.GraphFrameStats


class StatsController extends jfxf.Initializable {

  @jfxf.FXML private var graphModel: jfxsc.TextField = _
  @jfxf.FXML private var log: jfxsc.Label = _

  @jfxf.FXML private def startStats(event: jfxe.ActionEvent): Unit = {
    println(graphModel.getText)

    val runtimeSearch = GraphFrameStats.run(graphModel = graphModel.getText)
    log.setText(runtimeSearch)
  }


  override def initialize(location: URL, resources: ResourceBundle): Unit = {
  }
}

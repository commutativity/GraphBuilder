package frontend

import javafx.scene.layout.{AnchorPane => jAnchorPane, BorderPane => jBorderPane}
import javafx.{event => jfxe, fxml => jfxf}

import java.net.URL
import java.util.ResourceBundle

class HomeController extends jfxf.Initializable {

  // variables
  @jfxf.FXML private var borderpane: jBorderPane = _

  // switch scenes
  @jfxf.FXML def eventInfo(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Info.fxml"))
    borderpane.setCenter(view)
  }

  @jfxf.FXML def eventGraph(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Graph.fxml"))
    borderpane.setCenter(view)
  }

  @jfxf.FXML def eventStats(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Stats.fxml"))
    borderpane.setCenter(view)
  }

  @jfxf.FXML def eventShortestPaths(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Path.fxml"))
    borderpane.setCenter(view)
  }

  @jfxf.FXML def eventNeighbors(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Pattern.fxml"))
    borderpane.setCenter(view)
  }

  @jfxf.FXML def eventClickstream(event: jfxe.ActionEvent): Unit = {
    val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Property.fxml"))
    borderpane.setCenter(view)
  }


  override def initialize(url: URL, resources: ResourceBundle): Unit = {
      val view: jAnchorPane = jfxf.FXMLLoader.load(getClass.getResource("Info.fxml"))
      borderpane.setCenter(view)
  }
}

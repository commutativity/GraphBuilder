package frontend

import javafx.scene.control._
import javafx.scene.image._
import javafx.event._
import javafx.fxml._

import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.ResourceBundle
import analysis.{ShortestPaths, StartConversion}
import java.awt.Desktop
import java.io.File

class PathController extends Initializable {

  @FXML private var nameGraph: TextField = _
  @FXML private var startTextField: TextField = _
  @FXML private var endTextField: TextField = _
  @FXML private var runtimeOut: Label = _
  @FXML private var picture: ImageView = _
  @FXML private var openDirectory: Button = _


  @FXML private def openDirectory(event: ActionEvent): Unit = {
    val desktop = Desktop.getDesktop
    desktop.open(new File("D:\\scala_wiki\\sbt-test1\\src\\main\\resources\\gexf"))
  }

  val fileSuffix = "Path"

  @FXML private def submitShortestPath(event: ActionEvent): Unit = {
    val fileName = startTextField.getText + fileSuffix

    val runtimePath = ShortestPaths.run(
      nameGraph.getText,
      startTextField.getText,
      endTextField.getText,
      fileName
    )
    runtimeOut.setText(runtimePath)

    StartConversion.run(fileName)

    var myImage = new Image(Files.newInputStream(Paths.get(("src\\main\\resources\\png\\%s.png").format(fileName))))
    picture.setImage(myImage)

  }

  @FXML private def startConversion(event: ActionEvent): Unit = {
  }


  override def initialize(location: URL, resources: ResourceBundle): Unit = {
  }
}

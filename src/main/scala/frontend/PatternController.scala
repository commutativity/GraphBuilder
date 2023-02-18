package frontend

import javafx.scene.image._
import javafx.scene.control._
import javafx.event._
import javafx.fxml._

import java.net.URL
import java.util.ResourceBundle
import analysis.{PatternMatching, StartConversion}

import java.awt.Desktop
import java.io.File
import java.nio.file.{Files, Paths}

class PatternController extends Initializable {

  @FXML private var graphModel: TextField = _
  @FXML private var runtimeOut: Label = _
  @FXML private var picture: ImageView = _

  @FXML private var edge1: TextField = _
  @FXML private var edge2: TextField = _
  @FXML private var node1: TextField = _
  @FXML private var node2: TextField = _
  @FXML private var node3: TextField = _
  @FXML private var n1id: TextField = _
  @FXML private var n2id: TextField = _
  @FXML private var n3id: TextField = _
  @FXML private var n1ns: TextField = _
  @FXML private var n2ns: TextField = _
  @FXML private var n3ns: TextField = _
  @FXML private var n1cs: TextField = _
  @FXML private var n2cs: TextField = _
  @FXML private var n3cs: TextField = _
  @FXML private var button1: Button = _
  @FXML private var button2: Button = _

  @FXML private var key: TextField = _
  @FXML private var key1: TextField = _
  @FXML private var property: TextField = _
  @FXML private var property1: TextField = _
  @FXML private var option: TextField = _
  @FXML private var option1: TextField = _
  @FXML private var value: TextField = _
  @FXML private var value1: TextField = _
  @FXML private var buttonFilter1: Button = _
  @FXML private var openDirectory: Button = _


  @FXML private def openDirectory(event: ActionEvent): Unit = {
    val desktop = Desktop.getDesktop
    desktop.open(new File("D:\\scala_wiki\\sbt-test1\\src\\main\\resources\\gexf"))
  }


  @FXML private def ExposeHide1(event: ActionEvent): Unit = {
    if (node2.isVisible) {
      button1.setText("+")
      edge1.setVisible(false)
      edge1.setText("")
      node2.setVisible(false)
      node2.setText("")
      edge2.setVisible(false)
      edge2.setText("")
      node3.setVisible(false)
      node3.setText("")
    } else {
      button1.setText("-")
      edge1.setVisible(true)
      edge1.setText("edge1")
      node2.setVisible(true)
      node2.setText("node2")
    }
  }

  @FXML private def ExposeHide2(event: ActionEvent): Unit = {
    if (node3.isVisible) {
      button2.setText("+")
      edge2.setVisible(false)
      edge2.setText("")
      node3.setVisible(false)
      node3.setText("")
    } else {
      button2.setText("-")
      edge1.setVisible(true)
      edge1.setText("edge1")
      node2.setVisible(true)
      node2.setText("node2")
      edge2.setVisible(true)
      edge2.setText("edge2")
      node3.setVisible(true)
      node3.setText("node3")
    }
  }

  // exposes or hides the second filter option
  @FXML private def FilterExposeHide1(event: ActionEvent): Unit = {
    if (key1.isVisible) {
      buttonFilter1.setText("+")
      key1.setVisible(false)
      key1.setText("")
      property1.setVisible(false)
      property1.setText("")
      option1.setVisible(false)
      option1.setText("")
      value1.setVisible(false)
      value1.setText("")
    } else {
      buttonFilter1.setText("-")
      key1.setVisible(true)
      property1.setVisible(true)
      option1.setVisible(true)
      value1.setVisible(true)
    }
  }


  @FXML private def runPattern(event: ActionEvent): Unit = {
    val runtimeSearch = PatternMatching.run(
      graphName = graphModel.getText,
      node1 = node1.getText,
      edge1 = edge1.getText,
      node2 = node2.getText,
      edge2 = edge2.getText,
      node3 = node3.getText,
      key = key.getText,
      property = property.getText,
      option = option.getText,
      value = value.getText,
      key1 = key1.getText,
      property1 = property1.getText,
      option1 = option1.getText,
      value1 = value1.getText,
      gexfName = value.getText)
    runtimeOut.setText(runtimeSearch)

    var myImage = new Image(Files.newInputStream(Paths.get("src\\main\\resources\\png\\%s.png".format(value.getText))))
    picture.setImage(myImage)
  }


  @FXML override def initialize(location: URL, resources: ResourceBundle): Unit = {
  }
}

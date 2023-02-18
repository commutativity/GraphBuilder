import javafx.fxml._
import javafx.scene._
import javafx.scene.image.Image
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage

import java.io.IOException

object Main extends JFXApp3 {

  import scalafx.Includes._

  def start(): Unit = {

    val resource = getClass.getResource("frontend/Home.fxml")
    if (resource == null) {
      throw new IOException("Cannot load resource")
    }

    val root: Parent = FXMLLoader.load(resource)

    stage = new PrimaryStage() {
      title = "Graph Builder"
      scene = new Scene(root)
    }
    stage.getIcons.add(new Image(getClass.getResourceAsStream("frontend/Images/logo.png")))
  }
}

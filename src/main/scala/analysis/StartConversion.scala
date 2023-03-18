package analysis

import graph.JavaDriver
import utils.timeFunctions.walltime

object StartConversion extends App {

  run("Engine")

  def run(name: String): String = {

    val (_, time: Long) = walltime {

      val svgPath = "src\\main\\resources\\svg\\" + name + ".svg"
      val pngPath = "src\\main\\resources\\png\\" + name + ".png"

      // import java classes
      class ScalaDriver extends JavaDriver {
        runGEXFtoSVG(name, name)

        runSVGtoPNG(svgPath, pngPath)
      }

      // run imported java classes
      new ScalaDriver
    }

    val runtime: String = "%s seconds".format(time / 1000000000)
    runtime
  }
}

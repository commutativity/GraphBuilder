package utils

import System.nanoTime

object timeFunctions {
  def walltime[R](block: => R, time: Long = nanoTime): (R, Long) = {
    val t0 = nanoTime()
    val result = block
    val t1 = nanoTime()
    (result, t1 - t0)
  }


  def logParser(time: Long, mapEntities: String): Seq[String] = {
    val runtime = "%s seconds".format(time / 1000000000)
    var scriptLogs: Seq[String] = Seq()
    scriptLogs = scriptLogs :+ runtime :+ mapEntities
    scriptLogs
  }
}

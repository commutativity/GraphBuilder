package analysis

class EdgeClass {

  private var src = ""
  private var dst = ""
  private var internalClickstream: Option[Long] = _
  private var weight: Option[Double] = _



  def this(src: String, dst: String, internalClickstream: Option[Long], weight: Option[Double]) {
    this()
    this.src = src
    this.dst = dst
    this.internalClickstream = internalClickstream
    this.weight = weight

  }

  def getSrc: String = src

  def getDst: String = dst

  def getInternalClickstream: Option[Long] = internalClickstream

  def getWeight: Option[Double] = weight
}

package analysis

class VertexClass {

  private var id = ""
  private var redirectTitle = ""
  private var namespace = ""
  private var tags = ""
  private var category = ""
  private var clickstream: Option[Long] = _
  private var component: Option[Long] = _
  private var pagerank: Option[Double] = _


  def this(id: String, redirectTitle: String, namespace: String, tags: String, category: String,
           clickstream: Option[Long], component: Option[Long], pagerank: Option[Double]) {
    this()
    this.id = id
    this.redirectTitle = redirectTitle
    this.namespace = namespace
    this.tags = tags
    this.category = category
    this.clickstream = clickstream
    this.component = component
    this.pagerank = pagerank
  }


  def getId: String = id

  def getRedirectTitle: String = redirectTitle

  def getNamespace: String = namespace

  def getTags: String = tags

  def getCategory: String = category

  def getClickstream: Option[Long] = clickstream

  def getComponent: Option[Long] = component

  def getPagerank: Option[Double] = pagerank
}

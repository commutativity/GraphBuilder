package buildGraph

import graph.XMLInputFormat
import info.bliki.wiki.dump.{IArticleFilter, Siteinfo, WikiArticle, WikiXMLParser}
import info.bliki.wiki.filter.WikipediaParser
import info.bliki.wiki.model.WikiModel
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.htmlcleaner.HtmlCleaner
import org.xml.sax.SAXException

import java.io.ByteArrayInputStream

object Parser {

  /**
   * A helper class that allows for a WikiArticle to be serialized and also pulled from the XML parser
   *
   * @param page The WikiArticle that is being wrapped
   */
  case class WrappedPage(var page: WikiArticle = new WikiArticle) {}


  case class Page(title: String, text: String)

  /**
   * Helper class for parsing wiki XML, parsed pages are set in wrappedPage
   *
   */
  class SetterArticleFilter(val wrappedPage: WrappedPage) extends IArticleFilter {
    @throws(classOf[SAXException])
    def process(page: WikiArticle, siteinfo: Siteinfo) {
      wrappedPage.page = page
    }
  }

  /**
   * Represents a redirect from one wiki article title to another
   *
   * @param pageTitle     Title of the current article
   * @param redirectTitle Title of the article being redirected to
   */
  case class Redirect(pageTitle: String, redirectTitle: String)

  /**
   * Represent a link from one wiki article to another
   *
   * @param pageTitle Title of the current article
   * @param linkTitle Title of the linked article
   * @param row       Row the link shows on
   * @param col       Column the link shows up on
   */
  case class Link(pageTitle: String, linkTitle: String, row: Int, col: Int)


  /**
   * Represents a click from one wiki article to another.
   * https://datahub.io/dataset/wikipedia-clickstream
   * https://meta.wikimedia.org/wiki/Research:Wikipedia_clickstream
   *
   * @param prevTitle Title of the article click originated from if any
   * @param currTitle Title of the article the click went to
   * @param clickType Type of clicks, see documentation for more information
   * @param n         Number of clicks
   */
  case class Clicks(prevTitle: String, currTitle: String, clickType: String, n: Long)



  def readClickSteam(sc: SparkContext, file: String): RDD[Clicks] = {
    val rdd = sc.textFile(file, 12)
    rdd.zipWithIndex().map(_._1)
      .map(_.split('\t'))
      .map(l => Clicks(
        l(0), //Click stream uses _ for spaces while the dump parsing uses actual spaces
        l(1), //Click stream uses _ for spaces while the dump parsing uses actual spaces
        l(2),
        l(3).toLong)
      )
  }

  /**
   * Reads a wiki dump xml file, returning a single row for each <page>...</page>
   * https://en.wikipedia.org/wiki/Wikipedia:Database_download
   * https://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki
   */
  def readWikiDump(sc: SparkContext, file: String): RDD[(Long, String)] = {
    val conf = new Configuration()
    conf.set(XMLInputFormat.START_TAG_KEY, "<page>")
    conf.set(XMLInputFormat.END_TAG_KEY, "</page>")
    val rdd = sc.newAPIHadoopFile(file, classOf[XMLInputFormat], classOf[LongWritable], classOf[Text], conf)
    rdd.map { case (k, v) => (k.get(), new String(v.copyBytes())) }
  }

  /**
   * Parses the raw page text produced by readWikiDump into Page objects
   */
  def parsePages(rdd: RDD[(Long, String)]): RDD[(Long, Page)] = {
    rdd.mapValues {
      text => {
        val wrappedPage = new WrappedPage
        //The parser occasionally exceptions out, we ignore these
        try {
          val parser = new WikiXMLParser(new ByteArrayInputStream(text.getBytes),
            new SetterArticleFilter(wrappedPage))
          parser.parse()
        } catch {
          case e: Exception =>
        }
        val page = wrappedPage.page
        if (page.getTitle != null && page.getText != null) {
          Some(Page(page.getTitle, page.getText))
        } else {
          None
        }
      }
    }.flatMapValues(_.toSeq)
  }

  /**
   * Parses redirects out of the Page objects
   */
  def parseRedirects(rdd: RDD[Page]): RDD[Redirect] = {
    rdd.map {
      page =>
        val redirect =
          if (page.text != null) {
            val r = WikipediaParser.parseRedirect(page.text, new WikiModel("", ""))
            if (r == null) {
              None
            } else {
              Some(Redirect(page.title, r))
            }
          } else {
            None
          }
        redirect
    }.filter(_.isDefined).map(_.get)
  }

  /**
   * Parses internal article links from a Page object, filtering out links that aren't to articles
   */
  def parseInternalLinks(rdd: RDD[Page]): RDD[Link] = {
    rdd.flatMap {
      page =>
        if (page.text != null) {
          try {
            val html = WikiModel.toHtml(page.text)
            val cleaner = new HtmlCleaner
            val rootNode = cleaner.clean(html)
            val elements = rootNode.getElementsByName("a", true)
            val out = for (
              elem <- elements;
              classType = elem.getAttributeByName("class");
              title = elem.getAttributeByName("title")
              if (
                title != null
                  && !title.startsWith("User:") && !title.startsWith("User talk:")
                  && (classType == null || !classType.contains("external"))
                )
            ) yield {
              Link(page.title, StringEscapeUtils.unescapeHtml4(title), elem.getRow, elem.getCol)
            }
            out.toList
          } catch {
            case e: Exception => Nil
          }
        } else {
          Nil
        }
    }
  }
}

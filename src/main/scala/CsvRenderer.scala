import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}
import scala.util.parsing.combinator._
import play.twirl.api.Html

class CsvParser extends RegexParsers {
  override val skipWhitespace = false

  // field without double quotes
  def normalField = "[^,\r\n]*".r

  // filed with double quotes
  def quoteField = dblquote ~> ( ( "[^\"]".r | escDblquote ).* ^^ ( x => x.mkString ) ) <~ dblquote
  def dblquote = "\""

  // double quotes escape
  def escDblquote = "\"\"" ^^ ( x => "\"" )

  // single row
  def fields = repsep( quoteField | normalField, "," )

  // multi rows line
  def lines = repsep( fields | fields, eol )
  def eol = "\r\n" | "\n" | "\r"

  def parse( input: String) = parseAll( lines, input )
}

class CsvRenderer extends Renderer {

  def render(request: RenderRequest): Html = {
    import request._
    Html(toHtml(fileContent)(context))
  }

  def toHtml(fileContent: String)(implicit context: Context): String = {
    val path = context.baseUrl
    val parsed = new CsvParser().parse(fileContent).get

    var thead_ = ""
    var tbody_ = ""

    parsed.zipWithIndex.foreach{ case (row, cIndex) =>
      if (cIndex == 0) {
        thead_ += "<tr>"
        row.zipWithIndex.foreach{ case (v, rIndex) =>
          if (rIndex == 0) {
            thead_ += s"""<td class="index">${cIndex + 1}</td>"""
          } else {
            thead_ += s"""<th>$v</th>"""
          }
        }
        thead_ += "</tr>"
      } else {
        tbody_ += "<tr>"
        row.zipWithIndex.foreach{ case (v, rIndex) =>
          if (rIndex == 0) {
            tbody_ += s"""<td class="index">${cIndex + 1}</td>"""
          } else {
            tbody_ += s"""<td>$v</td>"""
          }
        }
        tbody_ += "</tr>"
      }
    }

    val thead =
      s"""
        |<thead>
        |$thead_
        |</thead>
        |""".stripMargin

    val tbody =
      s"""
        |<tbody>
        |$tbody_
        |</tbody>
        |""".stripMargin

    s"""
       |<link rel="stylesheet" type="text/css" href="$path/plugin-assets/csv/style.css">
       |<script src="$path/plugin-assets/csv/style.js"></script>
       |<table class="csv-data">
       |$thead
       |$tbody
       |</table>
       |""".stripMargin

  }

  def shutdown(): Unit = {
  }

}

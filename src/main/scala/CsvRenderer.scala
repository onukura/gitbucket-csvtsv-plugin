import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}
import gitbucket.core.util.FileUtil
import play.twirl.api.Html

import scala.util.matching.Regex
import scala.util.parsing.combinator._
import scala.util.{Failure, Success, Try}

class CsvParser extends RegexParsers {
  override val skipWhitespace = false
  def normalField: Regex = "[^,\r\n]*".r  // field without double quotes
  def quoteField: Parser[String] = dblquote ~> ( ( "[^\"]".r | escDblquote ).* ^^ (x => x.mkString ) ) <~ dblquote  // filed with double quotes
  def dblquote = "\""
  def escDblquote: Parser[String] = "\"\"" ^^ (x => "\"" )  // double quotes escape
  def fields: Parser[List[String]] = repsep( quoteField | normalField, "," )  // single row
  def lines: Parser[List[List[String]]] = repsep( fields | fields, eol )  // multi rows line
  def eol: Parser[String] = "\r\n" | "\n" | "\r"
  def parse( input: String): ParseResult[List[List[String]]] = parseAll( lines, input )
}

class TsvParser extends CsvParser {
  override def normalField: Regex = "[^\t\r\n]*".r  // field without double quotes
  override def fields: Parser[List[String]] = repsep( quoteField | normalField, "\t" )  // single row
}

class CsvRenderer extends Renderer {

  def render(request: RenderRequest): Html = {
    import request._
    Html(Try(toHtml(filePath, fileContent)(context)) match {
      case Success(v) => v
      case Failure(e) => s"""<h2>Error</h2><div><pre>$e</pre></div>"""
    })
  }

  def toHtml(filePath: List[String], fileContent: String)(implicit context: Context): String = {
    val path = context.baseUrl
    val basename = filePath.last
    val ext = FileUtil.getExtension(basename.toLowerCase)

    var parsed: List[List[String]] = Nil
    if ( ext == "csv" ) {
      parsed = new CsvParser().parse(fileContent).get
    } else if ( ext == "tsv" ) {
      parsed = new TsvParser().parse(fileContent).get
    }

    val thead_ = new StringBuilder
    val tbody_ = new StringBuilder

    val maxRowToRender = 3000
    if (parsed.length > maxRowToRender) {
      return (
        s"""
          |<h2>Oversize</h2>
          |<div><pre>Sorry. This file is too big to render.</pre></div>
          |""".stripMargin
      )
    }

    parsed.zipWithIndex.foreach{ case (row, rIndex) =>
      if (rIndex == 0) {
        thead_.append("<tr>")
        row.zipWithIndex.foreach{ case (v, cIndex) =>
          if (cIndex == 0) {
            thead_.append(s"""<td class="index">${rIndex + 1}</td>""")
          }
          thead_.append(s"""<th>$v</th>""")
        }
        thead_.append("</tr>")
      } else {
        tbody_.append("<tr>")
        row.zipWithIndex.foreach{ case (v, cIndex) =>
          if (cIndex == 0) {
            tbody_.append(s"""<td class="index">${rIndex + 1}</td>""")
          }
          tbody_.append(s"""<td>$v</td>""")
        }
        tbody_.append("</tr>")
      }
    }

    val thead = new StringBuilder
    thead.append("<thead>")
    thead.append(thead_.toString)
    thead.append("</thead>")

    val tbody = new StringBuilder
    tbody.append("<tbody>")
    tbody.append(tbody_.toString)
    tbody.append("</tbody>")

    s"""
       |<link rel="stylesheet" type="text/css" href="$path/plugin-assets/csv/style.css">
       |<script src="$path/plugin-assets/csv/style.js"></script>
       |<script src="$path/plugin-assets/csv/preview.js"></script>
       |<table class="csv-table">
       |${thead.toString}
       |${tbody.toString}
       |</table>
       |""".stripMargin

  }

  def shutdown(): Unit = {
  }

}

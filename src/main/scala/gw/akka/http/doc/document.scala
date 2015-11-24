package gw.akka.http.doc

import gw.akka.http.doc.RestDoc.Settings
import gw.akka.http.doc.converter.{Request, Response}

object document {
  import generators._

  case class Test(request: Request, response: Response)
  case class Document(name: String, content: String)

  type Generator = Test => Seq[Document]
  type Extractor = Test => Document

  def generator(settings: Settings): Generator =
    generator(settings.ExtractorNames.map(extractor))

  def generator(extractors: Seq[Extractor]): Generator =
    (test: Test) => extractors.map(_(test))

  object generators {

    def extractor(name: String) = name match {
      case "request" => requestExt
      case "response" => responseExt
      case "curl" => curlExt
      case "path-parameters" => pathParametersExt
    }

    val requestExt: Extractor = (test: Test) => {
      val request = test.request

      Document(
        "http-request",
        s"""
           |[source, http]
           |----
           |${request.method} ${request.uri} ${request.protocol}
           |Host: ${request.host}
           |${request.headers.map(header => s"${header.name}: ${header.value}").mkString("\n").eolnIfNonEmpty}
           |${request.body}
           |----
        """.stripMargin
      )
    }

    val pathParametersExt: Extractor = (test: Test) => {
      val params = test.request.pathParams

      Document(
        "http-request-path-parameters",
        s"""
           |.Path parameters
           |[format="csv", options="header"]
           ||===
           |Name,Value,Type
           |${params.map(param => s"${param._1},${param._2},${param._2.getClass.getName}").mkString("\n")}
           ||===
         """.stripMargin
      )
    }

    val responseExt: Extractor = (test: Test) => {
      val response = test.response

      Document(
        "http-response",
        s"""
           |[source, http]
           |----
           |${response.protocol} ${response.status.code} ${response.status.message}
           |${response.headers.map(header => s"${header.name}: ${header.value}").mkString("\n").eolnIfNonEmpty}
           |${response.body}
           |----
        """.stripMargin
      )
    }

    val curlExt: Extractor = (test: Test) => {
      val request = test.request

      val content = request.body.lines.map(_.trim).mkString("")

      val headers = request.headers.map { header =>
        if ("Content-Length".eq(header.name)) {
          header.copy(value = content.length.toString)
        } else {
          header
        }
      }.map { header =>
        s"""--header "$header""""
      }.mkString("", " ", " ")

      val contentDescription = if (content.nonEmpty) {
        s"--data '$content' $headers "
      } else {
        ""
      }

      Document(
        "curl-request",
        s"""
           |[source,bash]
           |----
           |$$ curl -X ${request.method} $contentDescription'http://${request.host}${request.uri}' -i
           |----
        """.
          stripMargin
      )
    }

    implicit class Eolns(s: String) {
      def eolnIfNonEmpty = if (s.isEmpty) s else s + "\n"
    }
  }
}

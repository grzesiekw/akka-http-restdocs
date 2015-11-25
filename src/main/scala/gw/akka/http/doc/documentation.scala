package gw.akka.http.doc

import gw.akka.http.doc.converter.{Request, Response}

object documentation {
  case class Test(request: Request, response: Response)
  case class Document(name: String, content: String)

  type Generator = Test => Seq[Document]
  type Extractor = Test => Document

  def generator(extractors: Seq[Extractor]): Generator = (test: Test) => extractors.map(_(test))

  def generator(settings: RestDocSettings): Generator = generator(settings.ExtractorNames.map(extractor(_)))

  object extractor {
    val all = Seq(HttpRequestExtractor, HttpResponseExtractor, CurlExtractor, PathParametersExtractor)

    def apply(name: String): Extractor = all.find(_.name == name).get

    abstract class NamedExtractor(val name: String) extends Extractor

    object HttpRequestExtractor extends NamedExtractor("http-request") {
      override def apply(test: Test): Document = {
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
    }

    object HttpResponseExtractor extends NamedExtractor("http-response") {
      override def apply(test: Test): Document = {
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
    }

    object CurlExtractor extends NamedExtractor("curl-request") {
      override def apply(test: Test): Document = {
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
    }

    object PathParametersExtractor extends NamedExtractor("path-parameters") {
      override def apply(test: Test): Document = {
        val params = test.request.pathParams

        Document(
          "path-parameters",
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
    }

    private implicit class Eolns(s: String) {
      def eolnIfNonEmpty = if (s.isEmpty) s else s + "\n"
    }

  }
}

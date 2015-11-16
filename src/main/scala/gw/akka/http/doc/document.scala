package gw.akka.http.doc

import gw.akka.http.doc.converter.{Request, Response}

object document {

  case class Test(request: Request, response: Response)
  case class Document(name: String, kind: String, content: String)

  type Generator = Test => Seq[Document]
  type Extractor = Test => Document
  type Formatter = Document => Document

  def generator(extractors: Seq[Extractor])(formatter: Formatter): Generator =
    (test: Test) => extractors.map(_.andThen(formatter)(test))

  object generator {

    val requestExt: Extractor = (test: Test) => {
      val request = test.request

      Document(
        "http-request",
        "http",
        s"""
           |${request.method} ${request.uri} ${request.protocol}
           |Host: ${request.host}
           |${request.headers.map(header => s"${header.name}: ${header.value}").mkString("\n")}
           |${request.body}
        """.stripMargin
      )
    }

    val responseExt: Extractor = (test: Test) => {
      val response = test.response

      Document(
        "http-response",
        "http",
        s"""
           |${response.protocol} ${response.status.code} ${response.status.message}
           |${response.headers.map(header => s"${header.name}: ${header.value}").mkString("\n")}
           |
           |${response.body}
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
        "bash",
        s"""
           |$$ curl -X ${request.method} $contentDescription'http://${request.host}${request.uri}' -i
        """.
          stripMargin
      )
    }
  }

  object asciidoctor {
    val extension = "adoc"

    val formatter: Formatter = (document: Document) => {
      document.copy(content =
        s"""
           |[source,${document.kind}]
           |----
           |${document.content}
           |----
         """.stripMargin
      )
    }
  }

}

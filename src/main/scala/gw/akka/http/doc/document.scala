package gw.akka.http.doc

import gw.akka.http.doc.converter.{DocRequest, DocResponse}

object document {

  case class Description(name: String)
  case class TestCase(description: Description, request: DocRequest, response: DocResponse)

  case class Details(name: String, kind: Symbol, source: Symbol, extension: String) {
    def path = name
    def file = s"${kind.name}.$extension"

  }
  case class Document(details: Details, content: String)

  type Generator = TestCase => Seq[Document]
  type Extractor = TestCase => Document
  type Formatter = Document => Document

  case class Settings private(extractors: Seq[Extractor], formatter: Formatter) {
    def generator: Generator = (testCase: TestCase) => extractors.map(_ (testCase)).map(formatter)
  }

  object Settings {
    import formatter._
    import generator._

    def default = Settings(Seq(request, response, curl), asciidoctor)
  }

  object generator {
    val bash = 'bash
    val http = 'http

    val requestKind = 'request
    val request: Extractor = (testCase: TestCase) => {
      val request = testCase.request

      Document(
        Details(testCase.description.name, requestKind, http, ""),
        s"""
           |${request.method} ${request.uri} ${request.protocol}
           |Host: ${request.host}
           |${request.headers.map(header => s"${header.name}: ${header.value}").mkString("\n")}
           |${request.body}
        """.stripMargin
      )
    }

    val responseKind = 'response
    val response: Extractor = (testCase: TestCase) => {
      val response = testCase.response

      Document(
        Details(testCase.description.name, responseKind, http, ""),
        s"""
           |${response.protocol} ${response.status.code} ${response.status.message}
           |${response.headers.map(header => s"${header.name}: ${header.value}").mkString("\n")}.
           |${response.body}
        """.stripMargin
      )
    }

    val curlKind = 'curl
    val curl: Extractor = (testCase: TestCase) => {
      val request = testCase.request

      val content = request.body.lines.map(_.trim).mkString("")

      val contentType = request.headers.find(header => header.name.equals("Content-Type")).map { contentType =>
        s"""--header "Content-Type: $contentType"""
      }.getOrElse("*/*")

      val contentDescription = if (content.nonEmpty) {
        s"--data '$content' $contentType "
      } else {
        ""
      }

      Document(
        Details(testCase.description.name, curlKind, bash, ""),
        s"""
           |$$ curl -X ${request.method}$contentDescription'http://${request.host}${request.uri}' -i
        """.
          stripMargin
      )
    }

    val generators = Map(requestKind -> request, responseKind -> response, curlKind -> curl)
  }

  object formatter {
    val asciidoctor: Formatter = (document: Document) => {
      document.copy(document.details.copy(extension = "adoc"), content =
        s"""
           |[source,${document.details.source.name}]
           |${document.content}
        """.stripMargin
      )
    }
  }

}

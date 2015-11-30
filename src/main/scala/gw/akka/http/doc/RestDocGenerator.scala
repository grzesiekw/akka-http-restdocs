package gw.akka.http.doc

case class Document(name: String, content: String)

trait Generator[A] {
  def generate(test: RestTest): A
}

class RestDocGenerator(generators: Seq[Generator[Document]]) extends Generator[Seq[Document]] {
  override def generate(test: RestTest): Seq[Document] = generators.map(_.generate(test))
}

object RestDocGenerator {
  import DocumentGenerator._

  def apply(generators: Seq[Generator[Document]]): Generator[Seq[Document]] = new RestDocGenerator(generators)

  def apply(settings: RestDocSettings): Generator[Seq[Document]] =
    apply(settings.ExtractorNames.map(name => knownGenerators.find(_.name == name).get))
}

abstract class NamedGenerator[A](val name: String) extends Generator[A]

object DocumentGenerator {

  val knownGenerators = Seq(HttpRequestGenerator, HttpResponseGenerator, CurlGenerator, PathParametersGenerator)

  object HttpRequestGenerator extends NamedGenerator[Document]("http-request") {
    override def generate(test: RestTest): Document = {
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

  object HttpResponseGenerator extends NamedGenerator[Document]("http-response") {
    override def generate(test: RestTest): Document = {
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

  object CurlGenerator extends NamedGenerator[Document]("curl-request") {
    override def generate(test: RestTest): Document = {
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

  object PathParametersGenerator extends NamedGenerator[Document]("path-parameters") {
    override def generate(test: RestTest): Document = {
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

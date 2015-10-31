package gw.akka.http.doc

import java.io.{File, BufferedWriter, FileWriter}
import java.nio.file.Paths

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.Config

trait RestDoc { this: RouteTest =>

  case class Settings(description: String = "default")

  def doc(description: String):(HttpRequest, RouteTestResult) => Unit = doc(Settings(description))

  def doc(settings: Settings): (HttpRequest, RouteTestResult) => Unit = (request, result) => {
    val docConfig = testConfig.getConfig("akka.http.doc")

    val outputDirectory = docConfig.getString("output-directory")
    val outputDirectoryFile = Paths.get(outputDirectory, settings.description).toFile

    if (!outputDirectoryFile.exists()) {
      outputDirectoryFile.mkdirs()
    }

    val outputDirectoryPath = outputDirectoryFile

    write(outputDirectoryPath, "curl-request.adoc", documentCurl(docConfig, request))
    write(outputDirectoryPath, "http-request.adoc", documentRequest(docConfig, request))
    write(outputDirectoryPath, "http-response.adoc", documentResult(docConfig, result))
  }

  private def write(directory: File, file: String, content: String): Unit = {
    val writer = new BufferedWriter(new FileWriter(new File(directory, file)))
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }

  private def documentCurl(config: Config, request: HttpRequest) = {
    s"""
      |[source,bash]
      |----
      |$$ curl 'http://${config.getString("host")}${request.uri}' -i
      |----
    """.stripMargin
  }

  private def documentRequest(config: Config, request: HttpRequest) = {
    s"""
      |[source,http]
      |----
      |${request.method.name} ${request.uri} ${request.protocol.value}
      |Host: ${config.getString("host")}
      |----
    """.stripMargin
  }

  private def documentResult(config: Config, result: RouteTestResult) = {
    val response = result.response

    s"""
      |[source,http]
      |----
      |${response.protocol.value} ${response.status.intValue()} ${response.status.defaultMessage()}
      |ContentType: ${response.entity.contentType()}
      |ContentLength: ${response.entity.contentLengthOption.map(_.toString).getOrElse("unknown")}
      |
      |${Unmarshal(response.entity).to[String].value.get.get}
      |----
    """.stripMargin
  }

  implicit class DocTransformation(requestWithResult: ((HttpRequest, RouteTestResult), Unit)) {
    def ~~>[A](f: (HttpRequest, RouteTestResult) => A) = {
      f(requestWithResult._1._1, requestWithResult._1._2)
    }
  }

  implicit class RequestValueWithTransformation[A](requestWithValue: (HttpRequest, A)) {
    def ~~>[B](f: A ⇒ B): ((HttpRequest, A), B) = {
      (requestWithValue, f(requestWithValue._2))
    }
  }

  implicit class RequestWithTransformation(request: HttpRequest) {
    def ~~>[A, B](f: A ⇒ B)(implicit ta: TildeArrow[A, B]): (HttpRequest, ta.Out) = {
      (request, ta(request, f))
    }
  }

}

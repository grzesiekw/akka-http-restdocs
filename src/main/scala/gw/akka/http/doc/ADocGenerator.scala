package gw.akka.http.doc

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths

import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.collection.immutable.Seq
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object ADocGenerator {

  private val duration = 1.second

  def generateDoc(config: Config, settings: Settings, request: HttpRequest, response: HttpResponse)(implicit materializer: Materializer) = {
    val outputDirectory = config.getString("output-directory")
    val outputDirectoryFile = Paths.get(outputDirectory, settings.description).toFile

    if (!outputDirectoryFile.exists()) {
      outputDirectoryFile.mkdirs()
    }

    val outputDirectoryPath = outputDirectoryFile

    write(outputDirectoryPath, "curl-request.adoc", documentCurl(config, request))
    write(outputDirectoryPath, "http-request.adoc", documentRequest(config, request))
    write(outputDirectoryPath, "http-response.adoc", documentResult(config, response))
  }

  private def write(directory: File, file: String, content: String): Unit = {
    val writer = new BufferedWriter(new FileWriter(new File(directory, file)))
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }

  private def documentCurl(config: Config, request: HttpRequest)(implicit materializer: Materializer) = {
    val method = if (!request.method.equals(HttpMethods.GET)) {
      s"-X ${request.method.name} "
    } else {
      ""
    }

    val content = documentContent(request.entity).lines.map(_.trim).mkString("")

    val dataHeader = if (content.nonEmpty) {
      s"""--header "Content-Type: ${request.entity.contentType}""""
    }

    request.headers.foreach { header =>
      println("curl H: " + header.name() + " " + header.value())
    }

    val data = if (content.nonEmpty) {
      s"--data '$content' $dataHeader "
    } else {
      ""
    }

    s"""
       |[source,bash]
       |----
       |$$ curl $method$data'http://${config.getString("host")}${request.uri}' -i
       |----
    """.stripMargin
  }

  private def documentRequest(config: Config, request: HttpRequest)(implicit materializer: Materializer) = {
    val headers = documentHeaders(request.headers)

    val content = documentContent(request.entity)

    s"""
       |[source,http]
       |----
       |${request.method.name} ${request.uri} ${request.protocol.value}
       |Host: ${config.getString("host")}
       |$headers
       |$content
       |----
    """.stripMargin
  }

  private def documentResult(config: Config, response: HttpResponse)(implicit materializer: Materializer) = {
    val entity = response.entity
    val content = documentContent(entity)
    val headers = documentHeaders(response.headers)

    s"""
       |[source,http]
       |----
       |${response.protocol.value} ${response.status.intValue()} ${response.status.defaultMessage()}
       |Content-Type: ${entity.contentType()}
       |Content-Length: ${entity.contentLengthOption.map(_.toString).getOrElse("unknown")}
       |$headers
       |$content
       |----
    """.stripMargin
  }

  def documentContent(entity: ResponseEntity)(implicit materializer: Materializer) =
    Await.result(entity.toStrict(duration).map(_.data.utf8String), duration)

  private def documentHeaders(headers: Seq[HttpHeader]) = headers.map(h => h.name() + ": " + h.value()).mkString("\n")
}

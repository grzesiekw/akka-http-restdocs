package gw.akka.http.doc

import akka.http.scaladsl.model.{ResponseEntity, HttpHeader, HttpResponse, HttpRequest}
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object converter {
  private val duration = 1.second

  case class Status(code: Int, message: String)
  case class Header(name: String, value: String)

  type Headers = Seq[Header]

  case class DocRequest(host: String, uri: String, protocol: String, method: String, headers: Headers, body: String)
  case class DocResponse(protocol: String, status: Status, headers: Headers, body: String)

  def request(config: Config, request: HttpRequest)(implicit materializer: Materializer): DocRequest =
    DocRequest(
      config.getString("host"),
      request.uri.toString(),
      request.protocol.value,
      request.method.name,
      headers(request.headers),
      entity(request.entity).content
    )

  def response(response: HttpResponse)(implicit materializer: Materializer): DocResponse = {
    val responseEntity = entity(response.entity)

    DocResponse(
      response.protocol.value,
      Status(response.status.intValue(), response.status.defaultMessage()),
      responseEntity.headers ++ headers(response.headers),
      responseEntity.content
    )
  }

  private def headers(headers: Seq[HttpHeader]) =
    headers.map(header => Header(header.name(), header.value()))

  case class Entity(contentType: String, content: String) {
    def headers = Seq(
      Header("Content-Type", contentType),
      Header("Content-Length", content.length.toString)
    )
  }

  private def entity(entity: ResponseEntity)(implicit materializer: Materializer): Entity =
    Await.result(entity.toStrict(duration).map(strict => Entity(entity.contentType().toString(), strict.data.utf8String)), duration)
}

package gw.akka.http.doc

import akka.http.scaladsl.model._
import akka.stream.Materializer

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object converter {
  private val duration = 1.second

  case class Status(code: Int, message: String)
  case class Header(name: String, value: String) {
    override def toString = s"$name: $value"
  }

  type Headers = Seq[Header]

  case class Request(host: String, uri: String, protocol: String, method: String, headers: Headers, body: String,
                    pathParams: Seq[(String, Any)])
  case class Response(protocol: String, status: Status, headers: Headers, body: String)

  def request(settings: RestDoc.Settings, restRequest: RestRequest)(implicit materializer: Materializer): Request = {
    val request = restRequest.request
    val pathParams = restRequest.params

    val requestEntity = entity(request.entity)

    Request(
      settings.Host,
      request.uri.toString(),
      request.protocol.value,
      request.method.name,
      requestEntity.headers ++ headers(request.headers),
      requestEntity.content,
      pathParams
    )
  }

  def response(response: HttpResponse)(implicit materializer: Materializer): Response = {
    val responseEntity = entity(response.entity)

    Response(
      response.protocol.value,
      Status(response.status.intValue(), response.status.defaultMessage()),
      responseEntity.headers ++ headers(response.headers),
      responseEntity.content
    )
  }

  private def headers(headers: Seq[HttpHeader]) =
    headers.map(header => Header(header.name(), header.value()))

  private case class Entity(contentType: ContentType, content: String) {

    def headers =
      Seq(
        (Header("Content-Type", contentType.toString()), ContentTypes.NoContentType.ne(contentType)),
        (Header("Content-Length", content.length.toString), content.length > 0)
      ).filter(_._2).map(_._1)

  }

  private def entity(entity: ResponseEntity)(implicit materializer: Materializer): Entity =
    Await.result(entity.toStrict(duration).map(strict => Entity(entity.contentType, strict.data.utf8String)), duration)

}

package gw.akka.http.doc

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpMethod}

case class RestRequest(request: HttpRequest, params: Seq[(String, String)])

trait RestRequestBuilding {

  class RestRequestBuilder(method: HttpMethod) {
    private def uriParams(uri: String) = "(\\{[^}]*})".r.findAllIn(uri).map(s => s.substring(1, s.length - 1)).to[Seq]

    def apply(uri: String, params: String*): RestRequest = {
      val paramsWithValues = uriParams(uri).zip(params)

      val fullUri = paramsWithValues.foldLeft(uri)((uri, paramWithValue) => uri.replace(s"{${paramWithValue._1}}", paramWithValue._2))

      RestRequest(HttpRequest(method, fullUri), paramsWithValues)
    }
  }

  val RestGet = new RestRequestBuilder(HttpMethods.GET)

}

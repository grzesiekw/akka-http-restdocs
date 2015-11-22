package gw.akka.http.doc

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._

case class RestRequest(request: HttpRequest, params: Seq[(String, Any)])

trait RestRequestBuilding {
  this: RequestBuilding =>

  implicit class ParametrizedRequestBuilder(request: HttpRequest) {
    private def uriParams(uri: String) = "(\\{[^}]*})".r.findAllIn(uri).map(s => s.substring(1, s.length - 1)).to[Seq]

    private def pathString(path: Path, fullPath: String): String = {
      if (path.isEmpty) {
        fullPath
      } else {
        pathString(path.tail, fullPath + path.head)
      }
    }

    def params(parameters: Any*) = {
      val uri = pathString(request.uri.path, "")

      val parametersWithValues = uriParams(uri.toString).zip(parameters)

      val fullUri = parametersWithValues.foldLeft(uri)((uri, paramWithValue) => uri.replace(s"{${paramWithValue._1}}", paramWithValue._2.toString))

      RestRequest(request.copy(uri = Uri(fullUri)), parametersWithValues)
    }
  }

}

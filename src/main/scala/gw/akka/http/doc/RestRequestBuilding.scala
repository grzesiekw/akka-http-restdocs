package gw.akka.http.doc

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._

import scala.annotation.tailrec

case class RestRequest(request: HttpRequest, params: Seq[(String, Any)])

trait RestRequestBuilding {

  implicit class ParametrizedRequestBuilder(request: HttpRequest) {

    def params(parameters: Any*) = {
      val (resolvedPath, namedParameters) = resolvePath(request.uri.path, parameters)

      RestRequest(request.copy(uri = request.uri.copy(path = resolvedPath)), namedParameters)
    }

    private type Params = Seq[(String, Any)]

    private def resolvePath(path: Path, parameters: Seq[Any]) = {
      @tailrec
      def go(path: Path, params: Seq[Any], resolvedPath: Path, namedParams: Params): (Path, Params) = {
        def stripBrackets(pathElement: String) = pathElement.substring(1, pathElement.length - 1)

        if (path.isEmpty) {
          (resolvedPath, namedParams)
        } else {
          val pathElement = path.head.toString

          if (pathElement.matches("^\\{[^}]+}")) {
            go(path.tail, params.tail, resolvedPath + params.head.toString, namedParams :+ (stripBrackets(pathElement), params.head))
          } else {
            go(path.tail, params, resolvedPath + pathElement, namedParams)
          }
        }
      }

      go(path, parameters, Path.Empty, Seq())
    }
  }

}

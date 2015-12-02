package gw.akka.http.doc

import akka.http.scaladsl.model.Uri.Query.{Cons, Empty}
import akka.http.scaladsl.model.Uri.{Query, Path}
import akka.http.scaladsl.model._

import scala.annotation.tailrec

case class RestRequest(request: HttpRequest, pathParams: Seq[(String, Any)] = Seq(), queryParams: Seq[(String, Any)] = Seq())

trait RestRequestBuilding {

  implicit class ParametrizedRequestBuilder(request: HttpRequest) {

    def params(parameters: Any*) = {
      val (resolvedPath, namedPathParameters) = resolvePath(request.uri, parameters)
      val (resolvedQuery, queryNamedParameters) = resolveQuery(request.uri.query(), parameters.drop(namedPathParameters.size))

      RestRequest(request.copy(uri = request.uri.withPath(resolvedPath).withQuery(resolvedQuery)), namedPathParameters, queryNamedParameters)
    }

    private type Params = Seq[(String, Any)]

    private def stripBrackets(pathElement: String) = pathElement.substring(1, pathElement.length - 1)

    private def resolvePath(path: Uri, parameters: Seq[Any]) = {
      @tailrec
      def go(path: Path, params: Seq[Any], resolvedPath: Path, namedParams: Params): (Path, Params) = {
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

      go(path.path, parameters, Path.Empty, Seq())
    }

    private def resolveQuery(query: Query, params: Seq[Any]) = {
      @tailrec
      def go(query: Query, params: Seq[Any], resolvedQuery: Query, namedParams: Params): (Query, Params) = {
        query match {
          case Empty => (resolvedQuery.reverse, namedParams)
          case Cons(key, value, tail) =>
            if (value.matches("^\\{[^}]+}")) {
              go(tail, params.tail, Cons(key, params.head.toString, resolvedQuery), namedParams :+ (stripBrackets(value), params.head.toString))
            } else {
              go(tail, params, Cons(key, value, resolvedQuery), namedParams)
            }

        }
      }

      go(query, params, Query.Empty, Seq())
    }
  }

}

package gw.akka.http.example.hello

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import spray.json.DefaultJsonProtocol

case class Name(value: String)

trait HelloProtocol extends DefaultJsonProtocol {
  implicit val nameFormat = jsonFormat1(Name)
}

trait HelloRoute extends HelloProtocol with SprayJsonSupport {

  implicit val materializer: Materializer

  val route =
    pathPrefix("hello") {
      (path(Segment) & get) { name =>
        complete {
          s"Hello $name!"
        }
      } ~
      (pathEnd & get) {
        complete {
          "Hello World!"
        }
      } ~
      post {
        entity(as[Name]) { name =>
          complete {
            if (name.value.length > 3) {
              OK
            } else {
              BadRequest
            }
          }
        }
      }
    }
}

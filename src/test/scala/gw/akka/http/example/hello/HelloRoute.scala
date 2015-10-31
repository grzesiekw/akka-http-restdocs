package gw.akka.http.example.hello

import akka.http.scaladsl.server.Directives._

trait HelloRoute {

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
      }
    }
}

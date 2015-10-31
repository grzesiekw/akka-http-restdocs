package gw.akka.http.doc

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.RouteTest
import gw.akka.http.doc.ADocGenerator.generateDoc

trait RestDoc { this: RouteTest =>

  def doc(description: String):(HttpRequest, RouteTestResult) => Unit = doc(Settings(description))

  def doc(settings: Settings): (HttpRequest, RouteTestResult) => Unit = (request, result) => {
    val config = testConfig.getConfig("akka.http.doc")

    generateDoc(config, settings, request, result.response)
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

case class Settings(description: String = "default")

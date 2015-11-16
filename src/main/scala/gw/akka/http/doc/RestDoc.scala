package gw.akka.http.doc

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.RouteTest

trait RestDoc { this: RouteTest =>
  import document._
  import generator._
  import asciidoctor._

  val gen = generator(Seq(requestExt, responseExt, curlExt))(formatter)

  def doc(name: String): (HttpRequest, RouteTestResult) => Unit = (request, result) => {
    val config = testConfig.getConfig("akka.http.doc")

    val writer = Writer(config.getString("output-directory"))

    val genRequest = converter.request(config, request)
    val genResponse = converter.response(result.response)

    gen(Test(genRequest, genResponse)).foreach { document =>
      writer.write(document, name, extension)
    }

  }

  implicit class DocTransformation(requestWithResult: ((HttpRequest, RouteTestResult), Unit)) {
    def ~~>[A](f: (HttpRequest, RouteTestResult) => A) = {
      f(requestWithResult._1._1, requestWithResult._1._2)
    }
  }

  implicit class RequestValueWithTransformation[A](requestWithValue: (HttpRequest, A)) {
    def ~~>[B](f: A => B): ((HttpRequest, A), B) = {
      (requestWithValue, f(requestWithValue._2))
    }
  }

  implicit class RequestWithTransformation(request: HttpRequest) {
    def ~~>[A, B](f: A => B)(implicit ta: TildeArrow[A, B]): (HttpRequest, ta.Out) = {
      (request, ta(request, f))
    }
  }

}
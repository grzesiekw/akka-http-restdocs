package gw.akka.http.doc

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.RouteTest
import com.typesafe.config.Config
import gw.akka.http.doc.RestDoc.Settings

trait RestDoc extends RestRequestBuilding {
  this: RouteTest =>

  import document._
  import writer._

  val settings = new Settings(testConfig)
  val gen = generator(settings)

  def doc(name: String): (RestRequest, RouteTestResult) => Unit = (request, result) => {
    val genRequest = converter.request(settings, request)
    val genResponse = converter.response(result.response)

    gen(Test(genRequest, genResponse)).foreach { document =>
      write(settings, name, document)
    }
  }

  implicit class DocTransformation[R](requestWithResult: ((R, RouteTestResult), Unit)) {
    def ~~>[A](f: (R, RouteTestResult) => A) = {
      f(requestWithResult._1._1, requestWithResult._1._2)
    }
  }

  implicit class RequestValueWithTransformation[A, R](requestWithValue: (R, A)) {
    def ~~>[B](f: A => B): ((R, A), B) = {
      (requestWithValue, f(requestWithValue._2))
    }
  }

  implicit class RequestWithTransformation(request: HttpRequest) {
    def ~~>[A, B](f: A => B)(implicit ta: TildeArrow[A, B]): (RestRequest, ta.Out) = {
      (RestRequest(request, Seq()), ta(request, f))
    }
  }

  implicit class RestRequestWithTransformation(request: RestRequest) {
    def ~~>[A, B](f: A => B)(implicit ta: TildeArrow[A, B]): (RestRequest, ta.Out) = {
      (request, ta(request.request, f))
    }
  }

}

object RestDoc {
  import scala.collection.JavaConversions._

  class Settings(config: Config) {
    val Host = config.getString("akka.http.doc.request.host")

    val ExtractorNames = config.getStringList("akka.http.doc.extractors").to[Seq]

    val FormatterExtension = config.getString("akka.http.doc.formatter.extension")

    val OutputDirectory = config.getString("akka.http.doc.writer.output-directory")
  }

}

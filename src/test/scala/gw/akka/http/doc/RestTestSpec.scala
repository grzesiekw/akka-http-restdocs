package gw.akka.http.doc

import akka.http.scaladsl.model.ContentTypes.`text/plain(UTF-8)`
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.Seq

class RestTestSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val settings = RestDocSettings(ConfigFactory.load())

  val body = "content"

  val test = RestTest(settings, RestRequest(Post("/hello", body).withHeaders(Seq(Accept(MediaTypes.`text/plain`)))), HttpResponse(OK, Seq(), Strict(`text/plain(UTF-8)`, ByteString(body))))

  "RestTest" should {
    "copy request uri" in {
      test.request.uri shouldBe "/hello"
    }

    "copy host from settings" in {
      test.request.host shouldBe "localhost:8080"
    }

    "copy request body" in {
      test.request.body shouldBe body
    }

    "copy request headers" in {
      test.request.headers should contain(Header("Accept", "text/plain"))
      test.request.headers should contain(Header("Content-Type", "text/plain; charset=UTF-8"))
      test.request.headers should contain(Header("Content-Length", body.length.toString))
    }

    "copy response status" in {
      test.response.status shouldBe Status(OK.intValue, OK.defaultMessage)
    }

    "copy response body" in {
      test.response.body shouldBe body
    }

    "copy response headers" in {
      test.response.headers should contain(Header("Content-Type", "text/plain; charset=UTF-8"))
      test.response.headers should contain(Header("Content-Length", body.length.toString))
    }
  }

}

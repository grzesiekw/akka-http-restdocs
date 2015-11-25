package gw.akka.http.doc

import akka.http.scaladsl.model.ContentTypes.`text/plain`
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import gw.akka.http.doc.converter.{Header, Status}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.Seq

class ConverterSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val settings = RestDocSettings(ConfigFactory.load())

  val body = "content"
  val docRequest = converter.request(settings, RestRequest(Post("/hello", body), Seq()))
  val docResponse = converter.response(HttpResponse(OK, Seq(), Strict(`text/plain`, ByteString(body))))

  "Request converter" should {
    "copy uri" in {
      docRequest.uri shouldBe "/hello"
    }

    "copy host from settings" in {
      docRequest.host shouldBe "localhost:8080"
    }

    "copy body" in {
      docRequest.body shouldBe body
    }
  }

  "Response converter" should {
    "copy status" in {
      docResponse.status shouldBe Status(OK.intValue, OK.defaultMessage)
    }

    "copy body" in {
      docResponse.body shouldBe body
    }

    "copy headers" in {
      docResponse.headers should contain(Header("Content-Type", "text/plain"))
      docResponse.headers should contain(Header("Content-Length", body.length.toString))
    }
  }

}

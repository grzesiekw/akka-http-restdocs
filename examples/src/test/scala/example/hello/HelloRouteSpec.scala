package example.hello

import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import gw.akka.http.doc.RestDoc
import org.scalatest.{Matchers, WordSpec}

class HelloRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with RestDoc
  with HelloRoute {

  "Hello service" should {
    "say hello to the World (default)" in {
      Get("/hello") ~> route ~> check {
        responseAs[String] shouldEqual "Hello World!"
      } ~> doc("hello")
    }

    "say hello to John" in {
      Get("/hello/John") ~> route ~> check {
        responseAs[String] shouldEqual "Hello John!"
      } ~> doc("hello-with-name")
    }

    "say hello to path John" in {
      Get("/hello/{name}").params("John") ~> route ~> check {
        responseAs[String] shouldEqual "Hello John!"
      } ~> doc("hello-with-path-name")
    }

    "say path Hi to path John" in {
      Get("/{message}/{name}").params("hi", "John") ~> route ~> check {
        responseAs[String] shouldEqual "Hi John!"
      } ~> doc("path-message-with-path-name")
    }

    "set default hello name" in {
      Post("/hello", Name("Jane")) ~> route ~> check {
        status shouldBe OK
      } ~> doc("hello-set-name")
    }

    "not set default hello too short name" in {
      Post("/hello", Name("Me")) ~> route ~> check {
        status shouldBe BadRequest
      } ~> doc("hello-set-too-short-name")
    }

    "say query Hi to query John" in {
      Get("/say?message={message}&name={name}&optional=true").params("Hi", "John") ~> route ~> check {
        responseAs[String] shouldEqual "Hi John!"
      } ~> doc("query-message-with-query-name")
    }
  }

}

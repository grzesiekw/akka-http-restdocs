package gw.akka.http.example.hello

import akka.http.scaladsl.testkit.ScalatestRouteTest
import gw.akka.http.doc.RestDoc
import org.scalatest.{Matchers, WordSpec}

class HelloRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with HelloRoute with RestDoc {

  "Hello service" should {
    "say hello to the World (default)" in {
      Get("/hello") ~~> route ~~> check {
        responseAs[String] shouldEqual "Hello World!"
      } ~~> doc("hello")
    }

    "say hello to John" in {
      Get("/hello/John") ~~> route ~~> check {
        responseAs[String] shouldEqual "Hello John!"
      } ~~> doc("hello-with-name")
    }
  }

}

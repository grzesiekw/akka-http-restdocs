package gw.akka.http.doc

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class RestDocSpec extends WordSpec with Matchers with ScalatestRouteTest with RestDoc {

  val route = path("test") {
    get {
      complete("OK")
    }
  }

  "RestDoc" should {
    "move request with result to doc" in {
      Get("/test") ~> route ~> check {

        status shouldBe OK
      } ~> doc("test")
    }
  }

}

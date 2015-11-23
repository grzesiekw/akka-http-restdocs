package gw.akka.http.doc

import akka.http.scaladsl.client.RequestBuilding
import org.scalatest.{Matchers, WordSpec}

class RestRequestBuildingSpec extends WordSpec with Matchers with RestRequestBuilding with RequestBuilding {

  val requestBuilder = new ParametrizedRequestBuilder(Get("/string/{a}/integer/{b}/boolean/{c}"))

  "ParametrizedRequestBuilder" should {
    "resolve parametrized uri" in {
      val restRequest = requestBuilder.params("A", 1, false)

      restRequest.request.uri.path.toString shouldBe "/string/A/integer/1/boolean/false"
    }

    "extract parameters" in {
      val restRequest = requestBuilder.params("A", 1, false)

      restRequest.params shouldBe Seq(("a", "A"), ("b", 1), ("c", false))
    }
  }

}

package gw.akka.http.doc

import org.scalatest.{Matchers, WordSpec}

class GeneratorSpec extends WordSpec with Matchers {

  import documentation._
  import extractor._
  import requests._

  "Request generator" should {
    "create all default documents" in {
      val documents = generator(extractor.all)(test)

      documents.map(_.name) shouldBe Seq("http-request", "http-response", "curl-request", "path-parameters")
    }

    "create request document" in {
      val document = HttpRequestExtractor(test)

      document.name shouldBe "http-request"
    }

    "create response document" in {
      val document = HttpResponseExtractor(test)

      document.name shouldBe "http-response"
    }

    "create curl request document" in {
      val document = CurlExtractor(test)

      document.name shouldBe "curl-request"
    }

    "create path parameters document" in {
      val document = PathParametersExtractor(test)

      document.name shouldBe "path-parameters"
    }
  }

}

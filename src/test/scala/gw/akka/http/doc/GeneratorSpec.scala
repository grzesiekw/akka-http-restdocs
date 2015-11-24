package gw.akka.http.doc

import org.scalatest.{Matchers, WordSpec}

class GeneratorSpec extends WordSpec with Matchers {

  import requests._
  import document._
  import generators._

  "Request generator" should {
    "create all default documents" in {
      val documents = generator(Seq(requestExt, responseExt, curlExt, pathParametersExt))(test)

      documents.map(_.name) shouldBe Seq("http-request", "http-response", "curl-request", "http-request-path-parameters")
    }

    "create request document" in {
      val document = requestExt(test)

      document.name shouldBe "http-request"
    }

    "create response document" in {
      val document = responseExt(test)

      document.name shouldBe "http-response"
    }

    "create curl request document" in {
      val document = curlExt(test)

      document.name shouldBe "curl-request"
    }

    "create path parameters document" in {
      val document = pathParametersExt(test)

      document.name shouldBe "http-request-path-parameters"
    }
  }

}

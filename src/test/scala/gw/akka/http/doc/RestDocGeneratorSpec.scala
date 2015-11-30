package gw.akka.http.doc

import gw.akka.http.doc.DocumentGenerator.knownGenerators
import org.scalatest.{Matchers, WordSpec}

class RestDocGeneratorSpec extends WordSpec with Matchers {

  import requests._

  "TestDocumentGenerator" should {
    "create all known documents" in {
      val documents = RestDocGenerator(knownGenerators).generate(test)

      documents.map(_.name) shouldBe Seq("http-request", "http-response", "curl-request", "path-parameters")
    }
  }

}

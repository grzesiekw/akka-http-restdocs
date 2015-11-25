package gw.akka.http.doc

import gw.akka.http.doc.converter.{Status, Response, Request}
import documentation.Test

object requests {

  val docRequest = Request("localhost:8080", "/test", "HTTP/1.1", "POST", Seq(), "content", Seq())
  val docResponse = Response("HTTP 1.1", Status(200, "OK"), Seq(), "content")

  val test = Test(docRequest, docResponse)

}

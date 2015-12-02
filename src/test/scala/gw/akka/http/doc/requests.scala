package gw.akka.http.doc

object requests {

  val docRequest = Request("localhost:8080", "/test", "HTTP/1.1", "POST", Seq(), "content")
  val docResponse = Response("HTTP 1.1", Status(200, "OK"), Seq(), "content")

  val test = RestTest(docRequest, docResponse)

}

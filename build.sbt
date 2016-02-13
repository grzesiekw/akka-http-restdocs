organization := "gw"

name := "akka-http-restdoc"

version := "0.1"

scalaVersion := "2.11.7"

val akkaHttpVersion = "2.0.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaHttpVersion,
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaHttpVersion,
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % akkaHttpVersion,

  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

triggeredMessage := Watched.clearWhenTriggered

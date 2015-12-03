organization := "gw"

name := "akka-http-restdocs"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0-M2",
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "2.0-M2",
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % "2.0-M2",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test",

  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)

enablePlugins(AsciidoctorPlugin)

sourceDirectory in Asciidoctor := file("src/asciidoctor")

triggeredMessage := Watched.clearWhenTriggered

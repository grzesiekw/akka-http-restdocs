name := "akka-http-restdoc"

val commonSettings = Seq(
  organization := "gw",
  version := "0.1",
  scalaVersion := "2.11.7"
)

val dependencies = {
  val akkaHttpVersion = "2.0.3"

  Seq(
    "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaHttpVersion,
    "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaHttpVersion,
    "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % akkaHttpVersion,
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}

lazy val core = (project in file("."))
  .settings(commonSettings)
  .settings(libraryDependencies ++= dependencies)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= dependencies)
  .dependsOn(core)

triggeredMessage := Watched.clearWhenTriggered

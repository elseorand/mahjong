val scalaV = "2.12.6"

lazy val commonSettings = Seq(
  organization := "com.elseorand",
  scalaVersion := scalaV,
  version := "0.1.0-SNAPSHOT",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
)

lazy val root = (project in file("."))
//  .aggregate(api, client)
  .settings(
    name := "mahjong",
    libraryDependencies := myLibraryDependencies,
    commonSettings
  )

val myLibraryDependencies = {
  val akkaV       = "2.5.15"
  val akkaHttpV   = "10.1.4"
  val scalaTestV = "3.0.5"
  val slickV = "3.2.3"
  val logV = "1.2.3"
  val scalaLogV = "3.9.0"

  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
    "com.h2database" % "h2" % "1.4.197",
    "ch.qos.logback" % "logback-classic" % logV,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLogV
  )
}

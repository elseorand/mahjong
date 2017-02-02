enablePlugins(ScalaJSPlugin)

name := """mahjong"""

version := "1.0"

scalaVersion := "2.12.1"

sbt.version=0.13.13

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-agent" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-camel" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-osgi" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-tck" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-distributed-data-experimental" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-typed-experimental" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-jackson" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % "10.0.1"
// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.193"
// https://mvnrepository.com/artifact/com.typesafe.slick/slick_2.11
libraryDependencies += "com.typesafe.slick" % "slick_2.11" % "3.1.1"
libraryDependencies += "com.typesafe.slick" % "slick-codegen_2.11" % "3.1.1"
// https://mvnrepository.com/artifact/com.jsuereth/scala-arm_2.11
libraryDependencies += "com.jsuereth" % "scala-arm_2.11" % "2.0"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"

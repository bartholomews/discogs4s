name := "discogs4s"

version := "0.1"

scalaVersion := "2.12.6"

val http4sVersion = "0.18.14"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.3",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "io.circe" % "circe-fs2_2.12" % "0.9.0",
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.9.3",
  // Optional for string interpolation to JSON model
  // "io.circe" %% "circe-literal" % "0.9.3"
)

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
// libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3"
)

// addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

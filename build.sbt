name := "discogs"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.6"

val http4sVersion = "0.18.14"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.3",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.typelevel" %% "cats-effect" % "0.10.1",
  "org.http4s" %% "http4s-dsl" % http4sVersion
    exclude("org.typelevel", "cats-effect_2.12"),
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "io.circe" % "circe-fs2_2.12" % "0.9.0"
    exclude("org.typelevel", "cats-core_2.12"),
  // auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.9.3"
    exclude("org.typelevel", "cats-core_2.12"),
  // string interpolation to JSON model
  // "io.circe" %% "circe-literal" % "0.9.3"
)

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
// libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3"
)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.18.0" % "test",
)

// http://www.scalatest.org/user_guide/using_scalatest_with_sbt
logBuffered in Test := false
parallelExecution in ThisBuild := false

// addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same line
  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
import sbt._

object Versions {
  val cats = "1.1.0"
  val circe = "0.10.0"
  val http4sVersion = "0.20.0-M4"
  val logback = "1.2.3"
  val pureConfig = "0.10.1"
}

object Dependencies {

  lazy val logback: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic",
    "ch.qos.logback" % "logback-core"
  ).map(_ % Versions.logback)

  lazy val pureConfig: Seq[ModuleID] = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-cats-effect"
  ).map(_ % Versions.pureConfig)

  lazy val typelevel: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % "1.3.3",
    "org.typelevel" %% "cats-effect" % Versions.cats,
    "org.http4s" %% "http4s-dsl" % Versions.http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Versions.http4sVersion,
    "io.circe" %% "circe-fs2" % Versions.circe,
    "io.circe" %% "circe-generic" % Versions.circe, // auto-derivation of JSON codecs
    "io.circe" %% "circe-literal" % Versions.circe // string interpolation to JSON model
  )

  val dependencies: Seq[ModuleID] =
    logback ++ pureConfig ++ typelevel

  lazy val testDependencies: Seq[ModuleID] = Seq(
  // https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
  // libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test
    "org.scalactic" %% "scalactic" % "3.0.5",
    // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
    "org.scalatest" %% "scalatest" % "3.0.5",
    "com.github.tomakehurst" % "wiremock" % "2.18.0",
  ).map(_ % Test)
}

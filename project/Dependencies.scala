import sbt._

object Versions {
  // https://github.com/typelevel/cats/releases
  val cats = "2.0.0"
  // https://github.com/typelevel/cats-effect/releases
  val cats_effect = "2.0.0"
  // https://github.com/circe/circe/releases
  val circe = "0.12.1"
  // https://github.com/circe/circe-fs2/releases
  val circe_fs2 = "0.12.0"
  // https://github.com/circe/circe-magnolia/releases
  val circe_magnolia = "0.4.0"
  // https://github.com/http4s/http4s/releases
  val http4sVersion = "0.21.0-M5"
  // https://github.com/lightbend/config/releases
  val lightbendConfig = "1.3.4"
  // https://github.com/pureconfig/pureconfig/releases
  val pureConfig = "0.12.1"
  // https://github.com/scalatest/scalatest/releases
  val scalaTest = "3.0.8"
  // https://github.com/tomakehurst/wiremock/releases
  val wiremock = "2.25.0"
}

object Dependencies {
  lazy val pureConfig: Seq[ModuleID] = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-cats-effect"
  ).map(_ % Versions.pureConfig)

  lazy val typelevel: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % Versions.lightbendConfig,
    "org.typelevel" %% "cats-effect" % Versions.cats_effect,
    "org.http4s" %% "http4s-dsl" % Versions.http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Versions.http4sVersion,
    "org.http4s" %% "http4s-circe" % Versions.http4sVersion,
    "io.circe" %% "circe-fs2" % Versions.circe_fs2,
    "io.circe" %% "circe-generic-extras" % Versions.circe,
    "io.circe" %% "circe-literal" % Versions.circe // string interpolation to JSON model
  )

  val dependencies: Seq[ModuleID] = pureConfig ++ typelevel ++ Seq(
    "io.bartholomews" %% "fsclient" % "0.0.1"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
    // libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test
    "org.scalactic" %% "scalactic" % Versions.scalaTest,
    // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
    "org.scalatest" %% "scalatest" % Versions.scalaTest,
    "com.github.tomakehurst" % "wiremock" % Versions.wiremock,
    // https://github.com/softwaremill/diffx/releases
    "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.12"
  ).map(_ % Test)

}

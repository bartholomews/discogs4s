import sbt._

object Versions {
  // https://github.com/scalatest/scalatest/releases
  val scalaTest = "3.0.8"
  // https://github.com/tomakehurst/wiremock/releases
  val wiremock = "2.25.0"
}

object Dependencies {

  val dependencies: Seq[ModuleID] = Seq(
    // https://mvnrepository.com/artifact/com.github.bartholomews/spotify-scala-client
    "io.bartholomews" %% "fsclient" % "0.0.1-SNAPSHOT"
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

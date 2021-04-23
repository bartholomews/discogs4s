import sbt._

object Dependencies {
  object Versions {
    // https://github.com/typelevel/cats
    val cats = "2.5.0"
    // https://github.com/bartholomews/fsclient
    val fsClient = "0.1.1+40-aa0ed2c6-SNAPSHOT"
    // https://github.com/xdotai/play-json-extensions
    val playJsonExtensions = "0.42.0"
  }

  val circeDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient-circe" % Versions.fsClient
  )

  val playDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient-play" % Versions.fsClient,
    "ai.x" %% "play-json-extensions" % Versions.playJsonExtensions
  )

  val dependencies: Seq[ModuleID] = Seq(
    "org.typelevel"   %% "cats-core"     % Versions.cats,
    "io.bartholomews" %% "fsclient-core" % Versions.fsClient
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % Versions.fsClient
  ).map(_ % Test)
}

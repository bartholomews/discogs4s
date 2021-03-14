import sbt._

object Dependencies {

  private val fsClientVersion = "0.1.1+21-52eadd08-SNAPSHOT"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient-circe" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.3+2-4ad35c91-SNAPSHOT"
  ).map(_ % Test)
}

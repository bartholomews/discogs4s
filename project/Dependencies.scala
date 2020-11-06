import sbt._

object Dependencies {

  private val fsClientVersion = "0.0.3"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.2+8-b48f2bb5-SNAPSHOT"
  ).map(_ % Test)
}

import sbt._

object Dependencies {

  private val fsClientVersion = "0.0.2+45-0393852a-SNAPSHOT"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % fsClientVersion
  ).map(_ % Test)
}

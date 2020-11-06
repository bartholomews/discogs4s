import sbt._

object Dependencies {

  private val fsClientVersion = "0.0.2+94-63b8456a-SNAPSHOT"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.2+5-6af2650f-SNAPSHOT" // fsClientVersion
  ).map(_ % Test)
}

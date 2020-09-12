import sbt._

object Dependencies {

  private val fsClientVersion = "0.0.2+45-0393852a-SNAPSHOT"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.2+4-daf79e02-SNAPSHOT" // fsClientVersion
  ).map(_ % Test)
}

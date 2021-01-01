import sbt._

object Dependencies {

  private val fsClientVersion = "0.1.0+1-b60c27b1+20201231-1048-SNAPSHOT" // "0.1.0"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient-circe" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.2+10-e954ea69+20201231-1436-SNAPSHOT"
  ).map(_ % Test)
}

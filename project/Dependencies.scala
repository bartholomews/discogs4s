import sbt._

object Dependencies {

  private val fsClientVersion = "0.1.0+0-2df98ab4+20210102-1839-SNAPSHOT"

  val dependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "fsclient-circe" % fsClientVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.3+1-439ed110+20210102-1855-SNAPSHOT"
  ).map(_ % Test)
}

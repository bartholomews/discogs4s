import CompilerOptions._
import Dependencies._
import sbt.Keys.{libraryDependencies, organization, parallelExecution}
import scoverage.ScoverageKeys.coverageFailOnMinimum

// https://github.com/scala/scala
ThisBuild / scalaVersion := "2.13.5"
inThisBuild(
  List(
    organization := "io.bartholomews",
    homepage := Some(url("https://github.com/batholomews/discogs4s")),
    licenses += ("GPL", url("http://opensource.org/licenses/GPL-3.0")),
    developers := List(
      Developer(
        "bartholomews",
        "Federico Bartolomei",
        "discogs4s@bartholomews.io",
        url("https://bartholomews.io")
      )
    )
  )
)

val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
  Test / logBuffered := false,
  Test / parallelExecution := false,
  Test / testOptions ++= TestSettings.options,
  resolvers +=
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

lazy val core = (project in file("modules/core"))
  .settings(
    commonSettings,
    name := "discogs4s-core",
    libraryDependencies ++= dependencies ++ testDependencies,
    coverageMinimum := 85,
    coverageFailOnMinimum := true
  )

lazy val circe = (project in file("modules/circe"))
  .dependsOn(core % "test->test; compile->compile")
  .settings(commonSettings)
  .settings(
    name := "discogs4s-circe",
    libraryDependencies ++= circeDependencies,
    coverageMinimum := 85,
    coverageFailOnMinimum := true
  )

lazy val play = (project in file("modules/play"))
  .dependsOn(core % "test->test; compile->compile")
  .settings(commonSettings)
  .settings(
    name := "discogs4s-play",
    libraryDependencies ++= playDependencies,
    coverageMinimum := 75,
    coverageFailOnMinimum := true
  )

// https://www.scala-sbt.org/1.x/docs/Multi-Project.html
// https://stackoverflow.com/questions/11899723/how-to-turn-off-parallel-execution-of-tests-for-multi-project-builds
lazy val discogs4s = (project in file("."))
  .settings(commonSettings)
  .settings(addCommandAlias("test", ";core/test;circe/test;play/test"): _*)
  .settings(publish / skip := true)
  .aggregate(core, circe, play)

addCommandAlias("test-coverage", ";clean ;coverage ;test ;coverageAggregate")
addCommandAlias("test-fast", "testOnly * -- -l org.scalatest.tags.Slow")

libraryDependencies ++= dependencies ++ testDependencies

coverageMinimum := 78 // FIXME
coverageFailOnMinimum := true

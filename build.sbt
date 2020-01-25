import CompilerOptions._
import Dependencies._

organization := "io.bartholomews"

name := "discogs4s"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.1"

resolvers += Resolver.bintrayRepo("bartholomews", "maven")

coverageMinimum := 3 // FIXME
coverageFailOnMinimum := true

libraryDependencies ++= dependencies ++ testDependencies

// http://www.scalatest.org/user_guide/using_scalatest_with_sbt
logBuffered in Test := false
parallelExecution in ThisBuild := false

scalacOptions ++= compilerOptions

val standardOutputReporter = Seq(
  "-oU"
)

val xmlReporter = Seq(
  "-u",
  "target/test-reports"
)

addCommandAlias("test-fast", "sbt testOnly * -l org.scalatest.tags.Slow")

//// TODO move into TestSettings
testOptions in Test ++= Seq(
  Tests.Argument(
    TestFrameworks.ScalaTest,
    standardOutputReporter ++ xmlReporter :_*
  )
)
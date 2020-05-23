import CompilerOptions._
import Dependencies._

name := "discogs4s"
scalaVersion := "2.13.2"

inThisBuild(List(
  organization := "io.bartholomews",
  homepage := Some(url("https://github.com/batholomews/discogs4s")),
  licenses += ("GPL", url("http://opensource.org/licenses/GPL-3.0")),
  developers := List(
    Developer(
      "bartholomews",
      "Federico Bartolomei",
      "fsclient@bartholomews.io",
      url("https://bartholomews.io")
    )
  )
))

libraryDependencies ++= dependencies ++ testDependencies

scalacOptions ++= compilerOptions

testOptions in Test ++= TestSettings.options
logBuffered in Test := false
parallelExecution in ThisBuild := false

coverageMinimum := 100
coverageFailOnMinimum := true

addCommandAlias("test-coverage", ";coverage ;test ;coverageReport")
addCommandAlias("test-fast", "testOnly * -l org.scalatest.tags.Slow")

import CompilerOptions._
import Dependencies._

name := "discogs4s"
scalaVersion := "2.13.2"

lazy val root = (project in file("."))
  .settings(TestSettings())
  .settings(
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
  ))

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= dependencies ++ testDependencies
scalacOptions ++= compilerOptions

testOptions in Test ++= TestSettings.options
logBuffered in Test := false
parallelExecution in ThisBuild := false

coverageMinimum := 55 // FIXME
coverageFailOnMinimum := true

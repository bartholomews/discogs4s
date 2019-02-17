import CompilerOptions._
import Dependencies._

name := "discogs4s"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= dependencies ++ testDependencies

// http://www.scalatest.org/user_guide/using_scalatest_with_sbt
logBuffered in Test := false
parallelExecution in ThisBuild := false

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

scalacOptions ++= compilerOptions

coverageEnabled := true
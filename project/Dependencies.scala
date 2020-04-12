import sbt._

object Dependencies {

  val dependencies: Seq[ModuleID] = Seq(
    // https://mvnrepository.com/artifact/com.github.bartholomews/spotify-scala-client
    "io.bartholomews" %% "fsclient" % "0.0.1-SNAPSHOT",
    // https://github.com/lloydmeta/enumeratum/releases
    "com.beachape" %% "enumeratum" % "1.5.15"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "io.bartholomews" %% "scalatestudo" % "0.0.1-SNAPSHOT"
  ).map(_ % Test)

}

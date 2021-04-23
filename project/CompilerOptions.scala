object CompilerOptions {

  // TODO plugin?
  lazy val compilerOptions: Seq[String] = Seq(
    "-Ymacro-annotations", // https://github.com/circe/circe/issues/975
    "-encoding", "utf8", // Option and arguments on same line
    "-Xfatal-warnings",  // New lines for each options
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  )
}

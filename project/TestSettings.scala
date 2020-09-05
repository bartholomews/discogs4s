import sbt.{Def, TestFrameworks, Tests, addCommandAlias}

object TestSettings {
  private val standardOutputReporter = Seq("-oU")
  private val xmlReporter = Seq("-u", "target/test-reports")

  val options = Seq(
    Tests.Argument(
      // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
      TestFrameworks.ScalaTest,
      standardOutputReporter ++ xmlReporter: _*
    )
  )

  def apply(): Def.SettingsDefinition =
    addCommandAlias("test-coverage", ";coverage ;test ;coverageReport") ++
    addCommandAlias("test-fast", "testOnly * -l org.scalatest.tags.Slow")
}

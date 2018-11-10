package features

import org.scalatest._

trait FeaturesTag extends FeatureSpec with BeforeAndAfterEachTestData {
  override def beforeEach(td: TestData) {
    markup(td.tags.map(tag => s"@$tag").mkString(" "))
    super.beforeEach(td)
  }
}

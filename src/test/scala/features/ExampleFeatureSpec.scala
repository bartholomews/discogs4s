package features

import org.scalatest._

import scala.collection.mutable.ListBuffer

class ExampleFeatureSpec extends FeatureSpec
  with GivenWhenThen
  with FeaturesTag
  with BeforeAndAfter
  with OneInstancePerTest {

  val jsonMarkup = new ListBuffer[(String, String)]

  before {
    markup("-" * 80)
  }

  after {
    jsonMarkup.foreach({ case (head, body) =>
      markup(s"#####$head")
      markup(s"`$body`")
    })
  }

  feature("Fast test is fast") {

    scenario("Fast test", Tag("Fast"), Tag("Slow"), Tag("Waa")) {
      Given("an empty stack")
      markup("`Markup for given json`")
      When("when pop is invoked on the stack")
      Then("NoSuchElementException should be thrown")
      And("the stack should still be empty")
      jsonMarkup.append {
        ("TEST IS FAST! Header", "The jon json")
      }
    }

    scenario("Slow test is slow", Tag("Slowo")) {
      Given("an empty stack")
      When("when pop is invoked on the stack")
      Then("NoSuchElementException should be thrown")
      And("the stack should still be empty")
      jsonMarkup.append {
        ("TEST IS SLOW! Header", "The jon json")
      }
    }

    //    info("As a programmer")
    //    info("I want to be able to pop items off the stack")
    //    info("So that I can get them in last-in-first-out order")
  }
}

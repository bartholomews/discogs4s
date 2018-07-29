package client

import org.http4s.Uri
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsOAuthClientSpec extends MockServerWordSpec with MockClientConfig with Matchers with PaginatedReleaseBehaviors {

  "Discogs OAuth Client" when {

    "getting authorization url" when {

      "consumer key is invalid" should {
        "return a Left with appropriate message" in {
          val response = clientWith("invalidConsumer").OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe Left("Invalid consumer.")
        }
      }

      "consumer secret is invalid" should {
        "return a Left with appropriate message" in {
          val response = clientWith(validConsumerKey, "invalidConsumerSecret")
            .OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe Left("Invalid signature. Please double check consumer secret key.")
        }
      }

      "consumer key and secret are valid" should {
        "return a Right with the callback Uri" in {
          val response = validOAuthClient.OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe Uri.fromString(
            "http://discogs.com/oauth/authorize?oauth_token=TOKEN"
          )
        }
      }
    }
  }
}
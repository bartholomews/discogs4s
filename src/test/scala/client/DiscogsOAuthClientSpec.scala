package client

import org.http4s.Uri
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsOAuthClientSpec extends MockServerWordSpec with MockClientConfig with Matchers { // with PaginatedReleaseBehaviors {

  "Discogs OAuth Client" when {

    "getting authorization url" when {

      "consumer key is invalid" should {
        val client = clientWith("invalidConsumer")
        "return a Left with appropriate message" in {
          val response = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe 'left
          response.left.get.getMessage shouldBe "Invalid consumer."
        }
      }

      "consumer secret is invalid" should {
        val client = clientWith(validConsumerKey, "invalidConsumerSecret")
        "return a Left with appropriate message" in {
          val response = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe 'left
          response.left.get.getMessage shouldBe
            "Invalid signature. Please double check consumer secret key."
        }
      }

      "consumer key and secret are valid" should {
        val client = validOAuthClient
        "return a Right with the callback Uri" in {
          val response = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
          response shouldBe Uri.fromString(
            "http://discogs.com/oauth/authorize?oauth_token=TOKEN"
          )
        }
      }

      "custom config has neither consumer application version nor url" should {
        val client = clientWith(appName = "some app", appVersion = None, appUrl = None)
        "have a proper USER-AGENT header" in {
          /*
            case class processUri() extends RequestF[OAuthRequest[Uri]] {
                 process(Request[IO]()).unsafeRunSync()
            }
           */
          // TODO: this should be done after wrapping every response in a new type
          // TODO: e.g. case class DiscogsResponse[T :< DiscogsEntity](status: Status, headers: Headers, entity: T)
          // TODO: having additional info like Headers(max-requests, user-agents), Status etc.
        }
      }
    }

    // TODO mock OAUTH to return empty response and assert returning ResponseError with that message
    // TODO mock OAUTH to return a 400 or something and assert returning ResponseError with that message
  }
}
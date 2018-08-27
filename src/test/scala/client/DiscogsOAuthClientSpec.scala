package client

import client.api.{AccessTokenRequest, OAuthResponse}
import org.http4s.{Status, Uri}
import org.http4s.client.oauth1.Token
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsOAuthClientSpec extends MockServerWordSpec with MockClientConfig with Matchers { // with PaginatedReleaseBehaviors {

  "Discogs OAuth Client" when {

    "getting authorization url" when {

      // TODO assert headers and status of HttpResponse

      "consumer key is invalid" when {
        val client = clientWith("invalidConsumer")
        def response: HttpResponse[OAuthResponse] = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
        "return a Left with appropriate message" in {
          response.entity shouldBe 'left
          response.entity.left.get.getMessage shouldBe "Invalid consumer."
        }
      }

      // TODO decode signature
//      "consumer secret is invalid" should {
//        val client = clientWith(validConsumerKey, "invalidConsumerSecret")
//        "return a Left with appropriate message" in {
//          val response = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
//          response.entity shouldBe 'left
//          response.entity.left.get.getMessage shouldBe
//            "Invalid signature. Please double check consumer secret key."
//        }
//      }

      "consumer key and secret are valid" when {
        val client = validOAuthClient
        def response: HttpResponse[OAuthResponse] = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
        "return a Right with the response Token" in {
          response.entity shouldBe 'right
          response.entity.right.get.token shouldBe Token("TOKEN", "SECRET")
        }
        "return a Right with the callback Uri" in {
          val response = client.OAUTH.getAuthoriseUrl.unsafeRunSync()
          response.entity shouldBe 'right
          response.entity.right.get.callback shouldBe Uri.unsafeFromString(
            "http://discogs.com/oauth/authorize?oauth_token=TOKEN"
          )
        }
      }

      "custom config has neither consumer application version nor url" when {
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

    "getting auth token" when {
      val client = validOAuthClient
      // TODO mock OAUTH to return empty response and assert returning ResponseError with that message
      // TODO mock OAUTH to return a 400 or something and assert returning ResponseError with that message
      // TODO handle other errors, look at ValidateTokenRequestBodyTransformer
      "request has an invalid verifier" when {
        val request = AccessTokenRequest(Token(validToken, validSecret), "invalidVerifier")
        def response: HttpResponse[OAuthResponse] = client.OAUTH.accessToken(request).unsafeRunSync()
        "return an error with the right code" in {
          response.entity shouldBe 'left
          response.entity.left.get.status shouldBe Status.BadRequest
        }
        "return an error with the right message" in {
          response.entity shouldBe 'left
          response.entity.left.get.getMessage shouldBe
            "Unable to retrieve access token. Your request token may have expired."
        }

        "request is valid" when {
          val request = AccessTokenRequest(Token(validToken, validSecret), validVerifier)
          def response: HttpResponse[OAuthResponse] = client.OAUTH.accessToken(request).unsafeRunSync()
          "return a response with Token and empty callbackConfirmed" in {
            response.entity shouldBe 'right
            val oAuthResponse = response.entity.right.get
            oAuthResponse.token shouldBe Token(validToken, validSecret)
            oAuthResponse.callbackConfirmed shouldBe None
          }
          "return a response with the right callback Uri" in {
            response.entity shouldBe 'right
            response.entity.right.get.callback shouldBe Uri.unsafeFromString(
              "http://discogs.com/oauth/authorize?oauth_token=TOKEN"
            )
          }
        }

      }
    }
  }
}
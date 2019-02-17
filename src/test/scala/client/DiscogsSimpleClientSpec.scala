package client

import client.effect4s.IOClient
import client.effect4s.entities.HttpResponse
import discogs.api.AccessTokenRequest
import discogs.DiscogsSimpleClient
import discogs.utils.Config.DiscogsConsumer
import discogs.entities.{AccessTokenResponse, RequestTokenResponse}
import org.http4s.{Status, Uri}
import org.http4s.client.oauth1.Token
import org.http4s.util.CaseInsensitiveString
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsSimpleClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient {

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration" should {
      "read the consumer values from resource folder" in {
        val client = new DiscogsSimpleClient()
        client.consumer.key shouldBe "mock-consumer-key"
        client.consumer.secret shouldBe "mock-consumer-secret"
      }
    }

    "initialised with an explicit configuration" should {

      val config = DiscogsConsumer(
        appName = "mock-app-name",
        appVersion = None,
        appUrl = None,
        key = "consumer-key",
        secret = "consumer-secret"
      )

      "read the consumer values from the injected configuration" in {
        val client = new DiscogsSimpleClient(config)
        client.consumer.key shouldBe "consumer-key"
        client.consumer.secret shouldBe "consumer-secret"
      }
    }

    "the client has valid consumer keys" when {

      val client: DiscogsSimpleClient = validClient

      "getting a request token" should {
        "succeed" in {
          val res = client.RequestToken.get.unsafeRunSync()
          res.status shouldBe Status.Ok
        }
      }

      "getting a oAuth client" should {

        "succeed" in {
          val oAuthClient = for {
            requestToken <- client.RequestToken.get.unsafeRunSync().entity
            res <- client.getOAuthClient(AccessTokenRequest(requestToken.token, validVerifier)).unsafeRunSync()
          } yield res

          oAuthClient shouldBe 'right
          oAuthClient.right.get.consumer shouldBe client.consumer
        }
      }


      "getting a request token" when {

        "consumer key is invalid" should {

          val client = clientWith("invalidConsumer")

          def response: HttpResponse[RequestTokenResponse] = client.RequestToken.get.unsafeRunSync()

          "return a Left with appropriate message" in {
            response.entity shouldBe 'left
            response.entity.left.get.getMessage shouldBe "Invalid consumer."
          }
        }

        "consumer secret is invalid" should {
          val client = clientWith(consumerWithInvalidSignature, "_")

          def response: HttpResponse[RequestTokenResponse] = client.RequestToken.get.unsafeRunSync()

          "return a Left with appropriate message" in {
            response.entity shouldBe 'left
            response.entity.left.get.getMessage shouldBe
              "Invalid signature. Please double check consumer secret key."
          }
        }

        "consumer key and secret are valid" should {

          val client = validClient

          def response: HttpResponse[RequestTokenResponse] = client.RequestToken.get.unsafeRunSync()

          "return a Right with the response Token" in {
            response.entity shouldBe 'right
            response.entity.right.get.token shouldBe Token("TOKEN", "SECRET")
          }
          "return a Right with the callback Uri" in {
            response.entity shouldBe 'right
            response.entity.right.get.callback shouldBe Uri.unsafeFromString(
              "http://127.0.0.1:8080/oauth/authorize?oauth_token=TOKEN"
            )
          }
        }

        "getting an unexpected response" should {

          val client = clientWith(consumerGettingUnexpectedResponse, "_")

          def response: HttpResponse[RequestTokenResponse] = client.RequestToken.get.unsafeRunSync()

          "return a Left with appropriate message" in {
            response.entity shouldBe 'left
            response.status shouldBe Status.BadRequest
            response.entity.left.get.getMessage shouldBe
              s"Unexpected response: $unexpectedResponse"
          }
        }

        "custom config has neither consumer application version nor url" should {

          val client = clientWith(appName = "some app", appVersion = None, appUrl = None)

          "have a proper USER-AGENT header" in {
            client.RequestToken.get.unsafeRunSync()
              .headers
              .get(CaseInsensitiveString("User-Agent"))
              .map(_.value) shouldBe Some("some app")
          }
        }
      }

      "getting an auth token" when {

        val client = validClient

        "request is empty" should {

          val request = AccessTokenRequest(Token(validToken, validSecret), emptyResponseMock)

          def response: HttpResponse[AccessTokenResponse] = client.AccessToken.get(request).unsafeRunSync()

          "return an error with the right code" in {
            response.entity shouldBe 'left
            response.entity.left.get.status shouldBe Status.BadRequest
          }
          "return an error with the right message" in {
            response.entity shouldBe 'left
            response.entity.left.get.getMessage shouldBe
              "Response was empty. Please check request logs"
          }

        }

        "request has an invalid verifier" should {
          val request = AccessTokenRequest(Token(validToken, validSecret), "invalidVerifier")

          def response: HttpResponse[AccessTokenResponse] = client.AccessToken.get(request).unsafeRunSync()

          "return an error with the right code" in {
            response.entity shouldBe 'left
            response.entity.left.get.status shouldBe Status.Unauthorized
          }
          "return an error with the right message" in {
            response.entity shouldBe 'left
            response.entity.left.get.getMessage shouldBe
              "Unable to retrieve access token. Your request token may have expired."
          }
        }

        "request is valid" should {

          def response: HttpResponse[AccessTokenResponse] = client.AccessToken.get(
            AccessTokenRequest(Token(validToken, validSecret), validVerifier)
          ).unsafeRunSync()

          "return a response with Token" in {
            response.entity shouldBe 'right
            val oAuthResponse = response.entity.right.get
            oAuthResponse.token shouldBe Token(validToken, validSecret)
          }
          "return a response with the right callback Uri" in {
            response.entity shouldBe 'right
          }
        }
      }

      "the client has invalid consumer keys" when {

        val client: DiscogsSimpleClient = clientWith("invalid-key")

        "getting a request token" should {
          "fail" in {
            val res = client.RequestToken.get.unsafeRunSync()
            res.status shouldBe Status.Unauthorized
          }
        }

        "getting a oAuth client" should {
          "fail" in {
            val oAuthClient = for {
              requestToken <- client.RequestToken.get.unsafeRunSync().entity
              res <- client.getOAuthClient(AccessTokenRequest(requestToken.token, validVerifier)).unsafeRunSync()
            } yield res
            oAuthClient shouldBe 'left
          }
        }
      }
    }
  }

}
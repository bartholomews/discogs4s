package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffMatcher
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthVersion.V1
import fsclient.entities.AuthVersion.V1.RequestToken
import fsclient.entities.{AuthEnabled, AuthVersion, HttpResponse}
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.entities.RequestTokenResponse
import io.bartholomews.discogs4s.wiremock.MockServer
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.{Status, Uri}
import org.scalatest.{Matchers, WordSpec}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends WordSpec with StubbedIO with MockClient with MockServer with Matchers with DiffMatcher {

  "DiscogsSimpleClient" when {

    // TODO: Should probably verify that client constructed is a `BasicSignatureClient`

    "initialised with an implicit configuration" should {
      "read the consumer values from resource folder" in {
        val discogs = new DiscogsClient()
        discogs.config.authInfo.signer.consumer.key shouldBe "mock-consumer-key"
        discogs.config.authInfo.signer.consumer.secret shouldBe "mock-consumer-secret"
      }
    }

    "initialised with an explicit configuration" should {

      val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)

      val config = FsClientConfig(
        userAgent = sampleUserAgent,
        authInfo = AuthEnabled(V1.BasicSignature(sampleConsumer))
      )

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(config)
        discogs.config.userAgent shouldBe sampleUserAgent
        discogs.config.authInfo.signer.consumer shouldBe Consumer(
          key = sampleConsumer.key,
          secret = sampleConsumer.secret
        )
      }
    }

    "getRequestToken" when {

      def request: IOResponse[RequestTokenResponse] = sampleClient.getRequestToken

      "the server responds with an error" should {

        def stub: StubMapping =
          stubFor(
            get(urlMatching("/oauth/request_token"))
              .willReturn(
                aResponse()
                  .withStatus(401)
                  .withBody("Invalid consumer.")
              )
          )

        "return a Left with appropriate message" in matchResponse(stub, request) {
          case _ @HttpResponse(_, Left(error)) =>
            error.status shouldBe Status.Unauthorized
            error.getMessage shouldBe "Invalid consumer."
        }
      }

      "the server responds with the expected string message" should {

        def stub: StubMapping =
          stubFor(
            get(urlMatching("/oauth/request_token"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withBody(
                    "oauth_token=TK1&oauth_token_secret=fafafafafaffafaffafafa&oauth_callback_confirmed=true"
                  )
              )
          )

        "return a Right with the response Token" in matchResponse(stub, request) {
          case _ @HttpResponse(_, Right(response)) =>
            response should matchTo(
              RequestTokenResponse(
                token = Token("TK1", "fafafafafaffafaffafafa"),
                callbackConfirmed = true
              )
            )
        }

        "return a Right with the callback Uri" in matchResponse(stub, request) {
          case _ @HttpResponse(_, Right(tokenResponse)) =>
            tokenResponse.callback shouldBe Uri.unsafeFromString(
              "http://127.0.0.1:8080/oauth/authorize?oauth_token=TK1"
            )
        }
      }

      "the server response is unexpected" should {

        def stub: StubMapping =
          stubFor(
            get(urlMatching("/oauth/request_token"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withBody(
                    "WAT"
                  )
              )
          )

        "return a Left with appropriate message" in matchResponse(stub, request) {
          case res @ HttpResponse(_, Left(error)) =>
            res.status shouldBe Status.UnprocessableEntity
            error.getMessage shouldBe "Unexpected response: WAT"
        }
      }
    }

    "getAccessToken" when {

      // FIXME: The implicit consumer passed here seems to be ignore,
      //  the actual token result will have the client consumer ???
      //  (should maybe pass the client instead of consumer?)
      def request: IOResponse[V1.AccessToken] = sampleClient.getAccessToken(
        RequestToken(sampleToken, tokenVerifier = "TOKEN_VERIFIER")(sampleConsumer)
      )

      "the server responds with an error" should {

        def stub: StubMapping =
          stubFor(
            post(urlMatching("/oauth/access_token"))
              .willReturn(
                aResponse()
                  .withStatus(401)
                  .withBody("Invalid consumer.")
              )
          )

        "return a Left with appropriate message" in matchResponse(stub, request) {
          case _ @HttpResponse(_, Left(error)) =>
            error.status shouldBe Status.Unauthorized
            error.getMessage shouldBe "Invalid consumer."
        }
      }

      "the server responds with the expected string message" should {

        def stub: StubMapping =
          stubFor(
            post(urlMatching("/oauth/access_token"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withBody(
                    "oauth_token=OATH_TK1&oauth_token_secret=TK_SECRET"
                  )
              )
          )

        "return a Right with the response Token" in matchResponse(stub, request) {

          case _ @HttpResponse(_, Right(response)) =>
            response should matchTo(
              AuthVersion.V1.AccessToken(
                Token(value = "OATH_TK1", secret = "TK_SECRET")
              )(sampleConsumer)
            )
        }
      }

      "the server response is unexpected" should {

        def stub: StubMapping =
          stubFor(
            post(urlMatching("/oauth/access_token"))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withBody(
                    "WAT"
                  )
              )
          )

        "return a Left with appropriate message" in matchResponse(stub, request) {
          case res @ HttpResponse(_, Left(error)) =>
            res.status shouldBe Status.UnprocessableEntity
            error.getMessage shouldBe "Unexpected response: WAT"
        }
      }
    }
  }
}

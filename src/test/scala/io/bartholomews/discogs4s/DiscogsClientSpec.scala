package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffMatcher
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthVersion.V1
import fsclient.entities.{AuthEnabled, HttpResponse}
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.entities.RequestTokenResponse
import io.bartholomews.discogs4s.wiremock.MockServer
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.{Status, Uri}
import org.scalatest.{Inside, Matchers}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec
    extends MockServer
    with MockClientConfig
    with Matchers
    with DiffMatcher
    with Inside
    with StubbedCall {

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
        authInfo = AuthEnabled(V1.BasicSignature(Consumer(key = "consumer-key", secret = "consumer-secret")))
      )

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(config)
        discogs.config.userAgent shouldBe sampleUserAgent
        discogs.config.authInfo.signer.consumer shouldBe Consumer(
          key = "consumer-key",
          secret = "consumer-secret"
        )
      }
    }

    "getRequestToken" when {

      def request: IOResponse[RequestTokenResponse] = sampleClient.getRequestToken

      "the server response is an error" should {

        def setupMocks: StubMapping =
          stubFor(
            get(urlMatching("/oauth/request_token"))
              .willReturn(
                aResponse()
                  .withStatus(401)
                  .withBody("Invalid consumer.")
              )
          )

        "return a Left with appropriate message" in insideResponse(setupMocks, request) {
          case _ @HttpResponse(_, Left(error)) =>
            error.status shouldBe Status.Unauthorized
            error.getMessage shouldBe "Invalid consumer."
        }
      }

      "the server response is the expected string message" should {

        def setupMocks(): StubMapping =
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

        "return a Right with the response Token" in insideResponse(setupMocks(), request) {
          case _ @HttpResponse(_, Right(response)) =>
            response should matchTo(
              RequestTokenResponse(
                token = Token("TK1", "fafafafafaffafaffafafa"),
                callbackConfirmed = true
              )
            )
        }

        "return a Right with the callback Uri" in insideResponse(setupMocks(), request) {
          case _ @HttpResponse(_, Right(tokenResponse)) =>
            tokenResponse.callback shouldBe Uri.unsafeFromString(
              "http://127.0.0.1:8080/oauth/authorize?oauth_token=TK1"
            )
        }
      }

      "the server response is unexpected" should {

        def setupMocks(): StubMapping =
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

        "return a Left with appropriate message" in insideResponse(setupMocks(), request) {
          case res @ HttpResponse(_, Left(error)) =>
            res.status shouldBe Status.UnprocessableEntity
            error.getMessage shouldBe "Unexpected response: WAT"
        }
      }
    }

    "getAccessToken" when {

      "request is empty" should {

        "return an error with the right code" in {}

        "return an error with the right message" in {}
      }

      "request has an invalid verifier" should {

        "return an error with the right code" in {}

        "return an error with the right message" in {}
      }
    }
  }

}

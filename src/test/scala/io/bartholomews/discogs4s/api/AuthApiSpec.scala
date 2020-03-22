package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import fsclient.entities.HttpResponse
import fsclient.entities.OAuthVersion.V1
import fsclient.entities.OAuthVersion.V1.RequestToken
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.StubbedWordSpec
import io.bartholomews.discogs4s.entities.RequestTokenResponse
import org.http4s.client.oauth1.Token
import org.http4s.{Status, Uri}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class AuthApiSpec extends StubbedWordSpec {

  "getRequestToken" when {

    def request: IOResponse[RequestTokenResponse] = sampleClient.auth.getRequestToken

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

    def request: IOResponse[V1.AccessToken] = sampleClient.auth.getAccessToken(
      RequestToken(sampleToken, verifier = "TOKEN_VERIFIER", sampleConsumer)
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
            V1.AccessToken(Token(value = "OATH_TK1", secret = "TK_SECRET"), sampleConsumer)
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
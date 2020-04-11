package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import fsclient.entities.OAuthVersion.Version1.{AccessTokenV1, RequestTokenV1}
import fsclient.entities.{FsResponseErrorJson, FsResponseErrorString, FsResponseSuccess}
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.StubbedWordSpec
import io.bartholomews.discogs4s.entities.{RequestToken, UserIdentity}
import org.apache.http.entity.ContentType
import org.http4s.client.oauth1.Token
import org.http4s.{Status, Uri}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class AuthApiSpec extends StubbedWordSpec {

  import io.circe.generic.auto._

  "getRequestToken" when {

    def request: IOResponse[RequestToken] = sampleClient.auth.getRequestToken

    "the server responds with an error" should {

      def stub: StubMapping =
        stubFor(
          get(urlMatching("/oauth/request_token"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withContentType(ContentType.TEXT_PLAIN)
                .withBody("Invalid consumer.")
            )
        )

      "return a Left with appropriate message" in matchResponse(stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.Unauthorized
          error shouldBe "Invalid consumer."
      }
    }

    "the server responds with the expected string message" should {

      def stub: StubMapping =
        stubFor(
          get(urlMatching("/oauth/request_token"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("oauth_token=TK1&oauth_token_secret=fafafafafaffafaffafafa&oauth_callback_confirmed=true")
            )
        )

      "return a Right with the response Token" in matchResponse(stub, request) {
        case FsResponseSuccess(_, _, response) =>
          response should matchTo(
            RequestToken(
              token = Token("TK1", "fafafafafaffafaffafafa"),
              callbackConfirmed = true
            )
          )
      }

      "return a Right with the callback Uri" in matchResponse(stub, request) {
        case FsResponseSuccess(_, _, tokenResponse) =>
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
                .withBody("WAT")
            )
        )

      "return a Left with appropriate message" in matchResponse(stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.UnprocessableEntity
          error shouldBe "Unexpected response: WAT"
      }
    }
  }

  "getAccessToken" when {

    def request: IOResponse[AccessTokenV1] = sampleClient.auth.getAccessToken(
      RequestTokenV1(sampleToken, verifier = "TOKEN_VERIFIER", sampleConsumer)
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
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.Unauthorized
          error shouldBe "Invalid consumer."
      }
    }

    "the server responds with the expected string message" should {

      def stub: StubMapping =
        stubFor(
          post(urlMatching("/oauth/access_token"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("oauth_token=OATH_TK1&oauth_token_secret=TK_SECRET")
            )
        )

      "return a Right with the response Token" in matchResponse(stub, request) {

        case FsResponseSuccess(_, _, response) =>
          response should matchTo(
            AccessTokenV1(Token(value = "OATH_TK1", secret = "TK_SECRET"), sampleConsumer)
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
                .withBody("WAT")
            )
        )

      "return a Left with appropriate message" in matchResponse(stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.UnprocessableEntity
          error shouldBe "Unexpected response: WAT"
      }
    }
  }

  "me" when {

    def request: IOResponse[UserIdentity] = sampleClient.auth.me(
      RequestTokenV1(sampleToken, verifier = "TOKEN_VERIFIER", sampleConsumer)
    )

    "the server responds with an error" should {

      def stub: StubMapping =
        stubFor(
          get(urlMatching("/oauth/identity"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withContentType(ContentType.APPLICATION_JSON)
                .withBodyFile("unauthenticated.json")
            )
        )

      "return a Left with appropriate message" in matchResponse(stub, request) {
        case FsResponseErrorJson(_, status, error) =>
          status shouldBe Status.Unauthorized
          error.as[DiscogsError].map(_.message) shouldBe Right("You must authenticate to access this resource.")
      }
    }

    "the server responds with the expected string message" should {

      def stub: StubMapping =
        stubFor(
          get(urlMatching("/oauth/identity"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("oauth/identity.json")
            )
        )

      "return a Right with the `UserIdentity` response" in matchResponse(stub, request) {

        case FsResponseSuccess(_, _, response) =>
          response shouldBe UserIdentity(
            id = 1L,
            username = "example",
            resourceUrl = Uri.unsafeFromString("https://api.discogs.com/users/example"),
            consumerName = "Your Application Name"
          )
      }
    }

    "the server response is unexpected" should {

      def stub: StubMapping =
        stubFor(
          get(urlMatching("/oauth/identity"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("WAT")
            )
        )

      "return a Left with appropriate message" in matchResponse(stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.UnprocessableEntity
          error shouldBe "There was a problem decoding or parsing this response, please check the error logs"
      }
    }
  }
}

package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.DiscogsWireWordSpec
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.{
  AccessTokenCredentials,
  ResourceOwnerAuthorizationUri,
  TemporaryCredentialsRequest
}
import io.bartholomews.scalatestudo.ServerBehaviours
import io.bartholomews.scalatestudo.data.ClientData.v1.{sampleConsumer, sampleToken}
import org.apache.http.entity.ContentType
import sttp.client3.{DeserializationException, HttpError, Identity, Response, ResponseException, UriContext}
import sttp.model.StatusCode

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
abstract class AuthApiSpec[E[_], D[_], DE, J] extends DiscogsWireWordSpec with ServerBehaviours[E, D, DE, J] {
  import DiscogsClientData._

  "getRequestToken" when {

    def request: Identity[Response[Either[ResponseException[String, Exception], TemporaryCredentials]]] =
      sampleOAuthClient.auth.getRequestToken(
        TemporaryCredentialsRequest(
          sampleConsumer,
          sampleRedirectUri,
          SignatureMethod.PLAINTEXT
        )
      )

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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(error), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          error shouldBe HttpError("Invalid consumer.", StatusCode.Unauthorized)
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

      "return a Right with the response Token" in matchResponseBody(stub, request) {
        case Right(response) =>
          response should matchTo(
            TemporaryCredentials(
              sampleConsumer,
              token = Token("TK1", "fafafafafaffafaffafafa"),
              callbackConfirmed = true,
              ResourceOwnerAuthorizationUri(uri"http://127.0.0.1:8080/oauth/authorize")
            )
          )
      }

      "return a Right with the callback Uri" in matchResponseBody(stub, request) {
        case Right(tokenResponse) =>
          tokenResponse.resourceOwnerAuthorizationRequest shouldBe
            uri"http://127.0.0.1:8080/oauth/authorize?oauth_token=TK1"
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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(DeserializationException(body, error)), status, _, _, _, _) =>
          body shouldBe "WAT"
          error.getMessage shouldBe "Unexpected response"
          status shouldBe StatusCode.Ok
      }
    }
  }

  "getAccessToken" when {

    def request: Response[Either[ResponseException[String, Exception], AccessTokenCredentials]] =
      sampleOAuthClient.auth.fromUri(
        sampleRedirectUri.value.withParams(
          Map(
            "oauth_token" -> sampleToken.value,
            "oauth_verifier" -> "SAMPLE_VERIFIER"
          )
        ),
        TemporaryCredentials(
          sampleConsumer,
          sampleToken,
          callbackConfirmed = true,
          resourceOwnerAuthorizationUri = ResourceOwnerAuthorizationUri(uri"http://127.0.0.1:8080")
        ),
        SignatureMethod.PLAINTEXT
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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(HttpError(body, _)), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          body shouldBe "Invalid consumer."
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

      "return a Right with the response Token" in matchResponseBody(stub, request) {
        case Right(response) =>
          response shouldBe AccessTokenCredentials(
            Token(value = "OATH_TK1", secret = "TK_SECRET"),
            sampleConsumer,
            SignatureMethod.PLAINTEXT
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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(DeserializationException(body, _)), status, _, _, _, _) =>
          status shouldBe StatusCode.Ok
          body shouldBe "WAT"
      }
    }
  }
}

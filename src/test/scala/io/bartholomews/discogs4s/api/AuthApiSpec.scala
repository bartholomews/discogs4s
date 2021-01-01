package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffMatcher.matchTo
import io.bartholomews.discogs4s.CoreWireWordSpec
import io.bartholomews.discogs4s.client.ClientData
import io.bartholomews.discogs4s.entities.UserIdentity
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.{
  AccessTokenCredentials,
  ClientCredentials,
  ResourceOwnerAuthorizationUri,
  TemporaryCredentialsRequest
}
import org.apache.http.entity.ContentType
import sttp.client.{DeserializationError, HttpError, Identity, Response, ResponseError, UriContext}
import sttp.model.StatusCode

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class AuthApiSpec extends CoreWireWordSpec {

  import ClientData._

  "getRequestToken" when {

    def request: Identity[Response[Either[ResponseError[Exception], TemporaryCredentials]]] =
      sampleClient.auth.getRequestToken(
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
        case Response(Left(error), status, _, _, _) =>
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
        case Response(Left(DeserializationError(body, error)), status, _, _, _) =>
          body shouldBe "WAT"
          error.getMessage shouldBe "Unexpected response"
          status shouldBe StatusCode.Ok
      }
    }
  }

  "getAccessToken" when {

    def request: Response[Either[ResponseError[Exception], AccessTokenCredentials]] =
      sampleClient.auth.fromUri(
        sampleRedirectUri.value.params(
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
        case Response(Left(HttpError(body, error)), status, _, _, _) =>
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
        case Response(Left(DeserializationError(body, error)), status, _, _, _) =>
          status shouldBe StatusCode.Ok
          body shouldBe "WAT"
      }
    }
  }

  "me" when {
    implicit val signer: ClientCredentials = sampleClient.client.signer
    def request: SttpResponse[io.circe.Error, UserIdentity] = sampleClient.users.me(signer)
    // TODO: Shouldn't only be enforce an `AccessTokenCredentials` as `SignerV1` ?
//      RequestTokenCredentials(sampleToken, verifier = "TOKEN_VERIFIER", sampleConsumer)

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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(HttpError(body, statusCode)), status, _, _, _) =>
          import io.circe.generic.auto._
          import io.circe.parser.parse
          status shouldBe StatusCode.Unauthorized
          parse(body).flatMap(_.as[DiscogsError]) shouldBe Right(
            DiscogsError(
              "You must authenticate to access this resource."
            )
          )
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

      "return a Right with the `UserIdentity` response" in matchResponseBody(stub, request) {

        case Right(response) =>
          response shouldBe UserIdentity(
            id = 1L,
            username = "example",
            resourceUrl = uri"https://api.discogs.com/users/example",
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

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(DeserializationError(body, error)), status, _, _, _) =>
          status shouldBe StatusCode.Ok
          body shouldBe "WAT"
      }
    }
  }
}

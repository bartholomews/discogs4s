package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.{DiscogsServerBehaviours, DiscogsWireWordSpec}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.{
  AccessTokenCredentials,
  ResourceOwnerAuthorizationUri,
  TemporaryCredentialsRequest
}
import io.bartholomews.scalatestudo.data.ClientData.v1.{sampleConsumer, sampleToken}
import sttp.client3.{Response, ResponseException, UriContext}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
abstract class AuthApiSpec[E[_], D[_], DE, J] extends DiscogsWireWordSpec with DiscogsServerBehaviours[E, D, DE, J] {
  import DiscogsClientData._

  "getRequestToken" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/oauth/request_token"))
    def request: SttpResponse[Exception, TemporaryCredentials] =
      sampleOAuthClient.auth.getRequestToken(
        TemporaryCredentialsRequest(
          sampleConsumer,
          sampleRedirectUri,
          SignatureMethod.PLAINTEXT
        )
      )

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns with the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("oauth_token=TK1&oauth_token_secret=TK_SECRET&oauth_callback_confirmed=true")
            )
        )

      "return a Token" in matchResponseBody(stub, request) { case Right(response) =>
        response should matchTo(
          TemporaryCredentials(
            sampleConsumer,
            token = Token("TK1", "TK_SECRET"),
            callbackConfirmed = true,
            ResourceOwnerAuthorizationUri(uri"http://127.0.0.1:8080/oauth/authorize")
          )
        )
      }

      "return a callback Uri" in matchResponseBody(stub, request) { case Right(tokenResponse) =>
        tokenResponse.resourceOwnerAuthorizationRequest shouldBe
          uri"http://127.0.0.1:8080/oauth/authorize?oauth_token=TK1"
      }
    }
  }

  "getAccessToken" when {
    def endpointRequest: MappingBuilder = post(urlPathEqualTo("/oauth/access_token"))
    def request: Response[Either[ResponseException[String, Exception], AccessTokenCredentials]] =
      sampleOAuthClient.auth.fromUri(
        sampleRedirectUri.value.withParams(
          Map(
            "oauth_token"    -> sampleToken.value,
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

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns with the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody("oauth_token=OATH_TK1&oauth_token_secret=TK_SECRET")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(response) =>
        response shouldBe AccessTokenCredentials(
          Token(value = "OATH_TK1", secret = "TK_SECRET"),
          sampleConsumer,
          SignatureMethod.PLAINTEXT
        )
      }
    }
  }
}

package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.DiscogsWireWordSpec
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.ClientCredentials
import io.bartholomews.scalatestudo.ServerBehaviours
import org.apache.http.entity.ContentType
import sttp.client3.{DeserializationException, HttpError, Response, UriContext}
import sttp.model.StatusCode

abstract class UsersApiSpec[E[_], D[_], DE, J] extends DiscogsWireWordSpec with ServerBehaviours[E, D, DE, J] {

  import DiscogsClientData._

  implicit def simpleUserDecoder: D[SimpleUser]
  implicit def userIdentityDecoder: D[UserIdentity]
  implicit def discogsErrorEncoder: E[DiscogsError]
  implicit def discogsErrorDecoder: D[DiscogsError]

  "getSimpleUserProfile" when {

    def request: SttpResponse[DE, SimpleUser] =
      sampleOAuthClient.users.getSimpleUserProfile(Username("rodneyfool"))(accessTokenCredentials)

    "the server responds with the response entity" should {

      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) {
        case Right(entity) =>
          entity should matchTo(
            SimpleUser(
              profile = "I am a software developer for Discogs.\r\n\r\n[img=http://i.imgur.com/IAk3Ukk.gif]",
              wantlistUrl = uri"https://api.discogs.com/users/rodneyfool/wants",
              rank = 149L,
              numPending = 61L,
              id = 1578108L,
              numForSale = 0L,
              homePage = UserWebsite(""),
              location = UserLocation("I live in the good ol' Pacific NW"),
              collectionFoldersUrl = uri"https://api.discogs.com/users/rodneyfool/collection/folders",
              username = Username("rodneyfool"),
              collectionFieldsUrl = uri"https://api.discogs.com/users/rodneyfool/collection/fields",
              releasesContributed = 5L,
              registered = "2012-08-15T21:13:36-07:00", // FIXME date
              ratingAvg = 3.47,
              numCollection = Some(78L),
              releasesRated = 116L,
              numLists = 0L,
              name = UserRealName("Rodney"),
              numWantlist = Some(160L),
              inventoryUrl = uri"https://api.discogs.com/users/rodneyfool/inventory",
              avatarUrl = uri"http://www.gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0?s=52&r=pg&d=mm",
              bannerUrl = Some(
                uri"https://img.discogs.com/dhuJe-pRJmod7hN3cdVi2PugEh4=/1600x400/filters:strip_icc():format(jpeg)/discogs-banners/B-1578108-user-1436314164-9231.jpg.jpg"
              ),
              uri = uri"http://www.discogs.com/user/rodneyfool",
              resourceUrl = uri"https://api.discogs.com/users/rodneyfool",
              buyerRating = 100.00,
              buyerRatingStars = 5,
              buyerNumRatings = 144L,
              sellerRatingStars = 5,
              sellerNumRatings = 21L,
              currAbbr = "USD"
            )
          )
      }
    }

    "the server responds with an error" should {

      def stub: StubMapping =
        stubFor(
          get(urlPathEqualTo("/users/rodneyfool"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withBody("Invalid consumer.")
            )
        )

      "decode an `Unauthorized` response" in matchIdResponse(stub, request) {
        case Response(Left(HttpError(body, statusCode)), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          statusCode shouldBe StatusCode.Unauthorized
          body shouldBe "Invalid consumer."
      }
    }
  }

  "me" when {
    implicit val signer: ClientCredentials = clientCredentials
    def request: SttpResponse[DE, UserIdentity] = sampleOAuthClient.users.me(signer)
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
        case Response(Left(HttpError(body, _)), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          val ec = entityCodecs[DiscogsError]
          ec.parse(body).flatMap(ec.decode) shouldBe Right(
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
        case Response(Left(DeserializationException(body, _)), status, _, _, _, _) =>
          status shouldBe StatusCode.Ok
          body shouldBe "WAT"
      }
    }
  }
}

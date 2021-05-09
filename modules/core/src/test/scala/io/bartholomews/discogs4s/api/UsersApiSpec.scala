package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.discogs4s.{DiscogsServerBehaviours, DiscogsWireWordSpec}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.ClientCredentials
import sttp.client3.UriContext

//noinspection MutatorLikeMethodIsParameterless
abstract class UsersApiSpec[E[_], D[_], DE, J] extends DiscogsWireWordSpec with DiscogsServerBehaviours[E, D, DE, J] {

  import DiscogsClientData._

  implicit def updateUserRequestEncoder: E[UpdateUserRequest]
  implicit def userProfileDecoder: D[UserProfile]
  implicit def userIdentityDecoder: D[UserIdentity]
  implicit def userSubmissionResponseDecoder: D[UserSubmissionResponse]
  implicit def userContributionsDecoder: D[UserContributions]

  private val sampleUserProfile = UserProfile(
    profile = "",
    wantlistUrl = uri"https://api.discogs.com/users/_.bartholomews/wants",
    rank = 0,
    numPending = 0,
    id = 2820336L,
    numForSale = 0,
    homePage = UserWebsite(""),
    location = UserLocation(""),
    collectionFoldersUrl = uri"https://api.discogs.com/users/_.bartholomews/collection/folders",
    username = Username("_.bartholomews"),
    collectionFieldsUrl = uri"https://api.discogs.com/users/_.bartholomews/collection/fields",
    releasesContributed = 0,
    registered = "2015-04-25T23:52:31-07:00", // FIXME date
    ratingAvg = 0.0,
    numCollection = Some(48),
    releasesRated = 0,
    numLists = 0L,
    name = UserRealName(""),
    numWantlist = Some(44),
    inventoryUrl = uri"https://api.discogs.com/users/_.bartholomews/inventory",
    avatarUrl = uri"https://secure.gravatar.com/avatar/60ab919f0b77cbd942d37bbdd9003607?s=500&r=pg&d=mm",
    bannerUrl = None,
    uri = uri"https://www.discogs.com/user/_.bartholomews",
    resourceUrl = uri"https://api.discogs.com/users/_.bartholomews",
    buyerRating = 100.00,
    buyerRatingStars = 5,
    buyerNumRatings = 7,
    sellerRatingStars = 0,
    sellerNumRatings = 0,
    currAbbr = "EUR",
    numUnread = Some(45),
    email = Some(UserEmail("discogs@bartholomews.io"))
  )

  "me" when {
    implicit val signer: ClientCredentials      = clientCredentials
    def endpointRequest: MappingBuilder         = get(urlPathEqualTo("/oauth/identity"))
    def request: SttpResponse[DE, UserIdentity] = sampleOAuthClient.users.me(signer)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("oauth/identity.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(response) =>
        response shouldBe UserIdentity(
          id = 1L,
          username = "example",
          resourceUrl = uri"https://api.discogs.com/users/example",
          consumerName = "Your Application Name"
        )
      }
    }
  }

  "getUserProfile" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/users/_.bartholomews"))
    def request: SttpResponse[DE, UserProfile] =
      sampleOAuthClient.users.getUserProfile(Username("_.bartholomews"))(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("users/_.bartholomews.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity should matchTo(sampleUserProfile)
      }
    }
  }

  "updateUserProfile" when {
    def endpointRequest: MappingBuilder = post(urlPathEqualTo("/users/_.bartholomews"))

    val updateUserRequest = UpdateUserRequest(
      name = Some(UserRealName("bartholomews")),
      homePage = Some(UserWebsite("https://bartholomews.io")),
      location = Some(UserLocation("London")),
      profile = Some(UserProfileInfo("")),
      currAbbr = Some(MarketplaceCurrency.GBP)
    )

    def request: SttpResponse[DE, UserProfile] =
      sampleOAuthClient.users
        .updateUserProfile(Username("_.bartholomews"), updateUserRequest)(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .withRequestBody(equalToJson("""
                                           |{
                                           |	"name": "bartholomews",
                                           |	"home_page": "https://bartholomews.io",
                                           |	"location": "London",
                                           |	"profile": "",
                                           |	"curr_abbr": "GBP"
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("users/_.bartholomews-updated.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity should matchTo(
          sampleUserProfile.copy(
            name = UserRealName("bartholomews"),
            homePage = UserWebsite("https://bartholomews.io"),
            location = UserLocation("London"),
            currAbbr = "GBP",
            email = Some(UserEmail("discogs@bartholomews.io"))
          )
        )
      }
    }
  }

  "getUserSubmissions" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/users/rodneyfool/submissions"))
      .withQueryParam("page", equalTo("1"))
      .withQueryParam("per_page", equalTo("50"))

    def request: SttpResponse[DE, UserSubmissionResponse] =
      sampleOAuthClient.users.getUserSubmissions(Username("rodneyfool"))(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("users/rodneyfool-submissions.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity.submissions.releases.size shouldBe 48
      }
    }
  }

  "getUserContributions" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/users/rodneyfool/contributions"))
      .withQueryParam("page", equalTo("1"))
      .withQueryParam("per_page", equalTo("50"))

    def request: SttpResponse[DE, UserContributions] =
      sampleOAuthClient.users.getUserContributions(Username("rodneyfool"))(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("users/rodneyfool-contributions.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity.contributions.size shouldBe 20
      }
    }
  }
}

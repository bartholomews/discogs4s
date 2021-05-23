package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.{DiscogsServerBehaviours, DiscogsWireWordSpec}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse

//noinspection MutatorLikeMethodIsParameterless
abstract class DatabaseApiSpec[E[_], D[_], DE, J]
    extends DiscogsWireWordSpec
    with DiscogsServerBehaviours[E, D, DE, J] {

  implicit def paginatedReleasesDecoder: D[PaginatedReleases]
  implicit def releaseDecoder: D[Release]
  implicit def releaseRatingDecoder: D[ReleaseRating]
  implicit def releaseRatingUpdateRequestEncoder: E[ReleaseRatingUpdateRequest]

  import DiscogsClientData._

  "getRelease" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/releases/249504"))
    def request: SttpResponse[DE, Release] =
      sampleOAuthClient.database.getRelease(releaseId = DiscogsReleaseId(249504))(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity on a request with default curr_abbr" should {
      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) { case Right(entity) =>
        entity.id shouldBe DiscogsReleaseId(249504)
        entity.blockedFromSale shouldBe false
      }
    }
  }

  "getReleaseRating" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/releases/249504/rating/_.bartholomews"))
    def request: SttpResponse[DE, ReleaseRating] =
      sampleOAuthClient.database.getReleaseRating(
        releaseId = DiscogsReleaseId(249504),
        username = DiscogsUsername("_.bartholomews")
      )(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity on a request with default curr_abbr" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("releases/get-rating.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity.releaseId shouldBe DiscogsReleaseId(249504)
        entity.username shouldBe DiscogsUsername("_.bartholomews")
        entity.rating shouldBe Rating.NoRating
      }
    }
  }

  "updateReleaseRating" when {
    def endpointRequest: MappingBuilder = put(urlPathEqualTo("/releases/249504/rating/_.bartholomews"))
    def request: SttpResponse[DE, ReleaseRating] =
      sampleOAuthClient.database.updateReleaseRating(
        ReleaseRatingUpdateRequest(
          username = DiscogsUsername("_.bartholomews"),
          releaseId = DiscogsReleaseId(249504),
          Rating.Five
        )
      )(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity on a request with default curr_abbr" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .withRequestBody(equalToJson("""
                                           |{
                                           |	"username": "_.bartholomews",
                                           |	"release_id": 249504,
                                           |	"rating": 5
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBodyFile("releases/get-rating.json")
            )
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case Right(entity) =>
        entity.releaseId shouldBe DiscogsReleaseId(249504)
        entity.username shouldBe DiscogsUsername("_.bartholomews")
        entity.rating shouldBe Rating.NoRating
      }
    }
  }

  "deleteReleaseRating" when {
    def endpointRequest: MappingBuilder = delete(urlPathEqualTo("/releases/249504/rating/_.bartholomews"))
    def request: SttpResponse[Nothing, Unit] =
      sampleOAuthClient.database.deleteReleaseRating(
        username = DiscogsUsername("_.bartholomews"),
        releaseId = DiscogsReleaseId(249504)
      )(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request, decodingBody = false))
    }

    "the server returns the expected response entity on a request with default curr_abbr" should {
      def stub: StubMapping =
        stubFor(
          endpointRequest
            .willReturn(aResponse().withStatus(204))
        )

      "decode the response correctly" in matchResponseBody(stub, request) { case response =>
        response shouldBe Right(())
      }
    }
  }

  "getArtistReleases" when {
    def endpointRequest: MappingBuilder = get(urlPathEqualTo("/artists/1/releases"))
      .withQueryParam("sort", equalTo("title"))
      .withQueryParam("sort_order", equalTo("asc"))

    def request: SttpResponse[DE, PaginatedReleases] =
      sampleOAuthClient.database.getArtistReleases(
        artistId = 1,
        sortBy = Some(SortBy.Title),
        sortOrder = Some(SortOrder.Asc)
      )(accessTokenCredentials)

    "something went wrong" should {
      behave.like(clientReceivingUnexpectedResponse(endpointRequest, request))
    }

    "the server returns the expected response entity on a request with sortBy and sortOrder" should {
      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) { case Right(entity) =>
        entity should matchTo(
          PaginatedReleases(
            pagination = Pagination(
              page = 1,
              pages = 103,
              items = 103,
              per_page = 1,
              urls = Some(
                PageUrls(
                  first = None,
                  prev = None,
                  next = Some("https://api.discogs.com/artists/1/releases?per_page=1&page=2"),
                  last = Some("https://api.discogs.com/artists/1/releases?per_page=1&page=103")
                )
              )
            ),
            releases = List(
              ArtistReleaseSubmission(
                status = Some("Accepted"),
                mainRelease = None,
                thumb = "",
                title = "Kaos",
                format = Some("10\""),
                label = Some("Svek"),
                role = "Main",
                year = Some(1997),
                resourceUrl = "https://api.discogs.com/releases/20209",
                artist = "Stephan-G* & The Persuader",
                `type` = "release",
                id = 20209
              )
            )
          )
        )
      }
    }

    "the server returns multiple entities on a request with default sortBy and sortOrder" should {
      def request: SttpResponse[DE, PaginatedReleases] =
        sampleOAuthClient.database.getArtistReleases(artistId = 1, sortBy = None, sortOrder = None)(
          accessTokenCredentials
        )

      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) { case Right(entity) =>
        entity.pagination.items shouldBe 110
        entity.pagination.per_page shouldBe 50
        inside(entity.releases.find(_.id == 12526186)) { case Some(release) =>
          release.year shouldBe None
        }
      }
    }
  }
}

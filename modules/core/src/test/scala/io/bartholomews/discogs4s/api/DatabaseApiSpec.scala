package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.{DiscogsServerBehaviours, DiscogsWireWordSpec}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse

abstract class DatabaseApiSpec[E[_], D[_], DE, J]
    extends DiscogsWireWordSpec
    with DiscogsServerBehaviours[E, D, DE, J] {

  implicit def paginatedReleasesDecoder: D[PaginatedReleases]

  import DiscogsClientData._

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
            releases = Seq(
              Release(
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

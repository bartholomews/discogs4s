package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.DiscogsWireWordSpec
import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.scalatestudo.ServerBehaviours
import sttp.client3.{HttpError, Response}
import sttp.model.StatusCode

abstract class ArtistsApiSpec[E[_], D[_], DE, J] extends DiscogsWireWordSpec with ServerBehaviours[E, D, DE, J] {

  implicit def paginatedReleasesDecoder: D[PaginatedReleases]

  import DiscogsClientData._

  "getArtistsReleases" when {

    "the server responds with one response entity" should {

      def request: SttpResponse[DE, PaginatedReleases] =
        sampleClient.artists.getArtistsReleases(artistId = 1,
                                                sortBy = Some(SortBy.Title),
                                                sortOrder = Some(SortOrder.Asc))

      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) {
        case Right(entity) =>
          entity should matchTo(
            PaginatedReleases(
              pagination = Pagination(
                page = 1,
                pages = 103,
                items = 103,
                per_page = 1,
                urls = PageUrls(
                  first = None,
                  prev = None,
                  next = "https://api.discogs.com/artists/1/releases?per_page=1&page=2",
                  last = "https://api.discogs.com/artists/1/releases?per_page=1&page=103"
                )
              ),
              releases = Seq(
                Release(
                  status = Some("Accepted"),
                  main_release = None,
                  thumb = "",
                  title = "Kaos",
                  format = Some("10\""),
                  label = Some("Svek"),
                  role = "Main",
                  year = Some(1997),
                  resource_url = "https://api.discogs.com/releases/20209",
                  artist = "Stephan-G* & The Persuader",
                  `type` = "release",
                  id = 20209
                )
              )
            )
          )
      }
    }

    "the server responds with multiple response entities" should {

      def request: SttpResponse[DE, PaginatedReleases] =
        sampleClient.artists.getArtistsReleases(artistId = 1, sortBy = None, sortOrder = None)

      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) {
        case Right(entity) =>
          entity.pagination.items shouldBe 110
          entity.pagination.per_page shouldBe 50
          inside(entity.releases.find(_.id == 12526186)) {
            case Some(release) => release.year shouldBe None
          }
      }
    }

    "the server responds with an error" should {

      def request: SttpResponse[DE, PaginatedReleases] =
        sampleClient.artists.getArtistsReleases(artistId = 1,
                                                sortBy = Some(SortBy.Year),
                                                sortOrder = Some(SortOrder.Desc))

      def stub: StubMapping =
        stubFor(
          get(urlPathEqualTo("/artists/1/releases"))
            .withQueryParam("sort", equalTo("year"))
            .withQueryParam("sort_order", equalTo("desc"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withBody("Invalid consumer.")
            )
        )

      "decode an `Unauthorized` response" in matchIdResponse(stub, request) {
        case Response(Left(error), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          error shouldBe HttpError("Invalid consumer.", StatusCode.Unauthorized)
      }
    }
  }
}

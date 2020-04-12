package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.client.MockClient
import io.bartholomews.discogs4s.entities.{PageUrls, PaginatedReleases, Pagination, Release, SortBy, SortOrder}
import io.bartholomews.fsclient.entities.{FsResponseErrorString, FsResponseSuccess}
import io.bartholomews.fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.testudo.WireWordSpec
import org.http4s.Status

class ArtistsApiSpec extends WireWordSpec with MockClient {

  "getArtistsReleases" when {

    "the server responds with the response entity" should {

      def request: IOResponse[PaginatedReleases] =
        sampleClient.artists.getArtistsReleases(artistId = 1,
                                                sortBy = Some(SortBy.Title),
                                                sortOrder = Some(SortOrder.Asc))

      "decode the response correctly" in matchResponse(stubWithResourceFile, request) {
        case FsResponseSuccess(_, _, entity) =>
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
                  year = 1997,
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

    "the server responds with an error" should {

      def request: IOResponse[PaginatedReleases] =
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

      "decode an `Unauthorized` response" in matchResponse[PaginatedReleases](stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.Unauthorized
          error shouldBe "Invalid consumer."
      }
    }
  }
}

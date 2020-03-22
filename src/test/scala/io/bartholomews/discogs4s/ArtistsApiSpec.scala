package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffMatcher
import fsclient.entities.HttpResponse
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.client.{MockClient, StubbedIO}
import io.bartholomews.discogs4s.entities.{PageUrls, PaginatedReleases, Pagination, Release, SortBy, SortOrder}
import io.bartholomews.discogs4s.server.MockServer
import org.http4s.Status
import org.scalatest.{Matchers, WordSpec}

class ArtistsApiSpec extends WordSpec with StubbedIO with MockServer with MockClient with Matchers with DiffMatcher {

  "Discogs OAuth Client" when {

    "getting Artists releases" when {

      "the server responds with the response entity" should {

        def request: IOResponse[PaginatedReleases] =
          sampleClient.artists.getArtistsReleases(artistId = 1,
                                                  sortBy = Some(SortBy.Title),
                                                  sortOrder = Some(SortOrder.Asc))

        "decode the response correctly" in matchResponse(stubWithResourceFile, request) {
          case HttpResponse(_, Right(entity)) =>
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

        "decode an `Unauthorized` response" in matchResponse(stub, request) {
          case response @ HttpResponse(_, Left(error)) =>
            response.status shouldBe Status.Unauthorized
            error.getMessage shouldBe "Invalid consumer."
        }
      }
    }
  }
}

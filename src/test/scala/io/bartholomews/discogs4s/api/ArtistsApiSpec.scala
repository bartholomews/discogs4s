package io.bartholomews.discogs4s.api

import com.softwaremill.diffx.scalatest.DiffMatcher
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthVersion.V1
import fsclient.entities.{AuthEnabled, HttpResponse}
import io.bartholomews.discogs4s.entities.{PageUrls, PaginatedReleases, Pagination, Release}
import io.bartholomews.discogs4s.wiremock.MockServer
import io.bartholomews.discogs4s.{DiscogsClient, MockClientConfig}
import org.http4s.Status
import org.http4s.client.oauth1.Consumer
import org.scalatest.{Inside, Matchers}

class ArtistsApiSpec extends MockServer with MockClientConfig with Matchers with DiffMatcher with Inside {

  "Discogs OAuth Client" when {

    "client is valid" should {

      val client =
        new DiscogsClient(
          FsClientConfig(
            userAgent = UserAgent(appName = "appName", appVersion = Some("appVersion"), appUrl = Some("appUrl")),
            authInfo = AuthEnabled(V1.BasicSignature(Consumer(key = "", secret = "")))
          )
        )

      "getting Artists releases" should {
        def res: HttpResponse[PaginatedReleases] =
          client.getArtistsReleases(artistId = 1, page = 1, perPage = 1).unsafeRunSync()

        "decode the response correctly" ignore {
          inside(res) {
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
      }
    }

    "client is invalid" should {

      val client = clientWith("invalid-key")

      "get an error response" ignore {

        def res: HttpResponse[PaginatedReleases] =
          client
            .getArtistsReleases(artistId = 1, page = 1, perPage = 1)
            .unsafeRunSync()

        inside(res) {
          case response @ HttpResponse(_, Left(error)) =>
            response.status shouldBe Status.Unauthorized
            error.getMessage shouldBe "Invalid consumer."
        }
      }
    }
  }
}

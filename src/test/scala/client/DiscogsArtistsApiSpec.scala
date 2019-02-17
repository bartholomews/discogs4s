package client

import client.effect4s.IOClient
import client.effect4s.entities.{HttpResponse, ResponseError}
import discogs.entities.PaginatedReleases
import org.http4s.Status
import org.scalatest.Matchers
import server.MockServerWordSpec

class DiscogsArtistsApiSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient {

  "Discogs OAuth Client" when {

    "client is valid" should {

      val client = validClient

      "getting Artists releases" should {

        def res: Either[ResponseError, PaginatedReleases] = client.getArtistsReleases(artistId = 1, perPage = 1)
          .unsafeRunSync()
          .entity

        "be a right" in {
          res shouldBe 'right
        }

        "have proper pagination" in {
          res.right.get.pagination.page shouldBe 1
        }
        "decode artist object" in {
          res.right.get.releases.head.artist shouldBe "Stephan-G* & The Persuader"
        }
      }
    }

    "client is invalid" should {

      val client = clientWith("invalid-key")

      "get an error response" in {

        def res: HttpResponse[PaginatedReleases] = client.getArtistsReleases(artistId = 1, perPage = 1)
          .unsafeRunSync()

        res.status shouldBe Status.Unauthorized
        res.entity shouldBe 'left
        res.entity.left.get.getMessage shouldBe "Invalid consumer."
      }

    }

  }

}

package client

import client.http.IOClient
import entities.{PaginatedReleases, ResponseError}
import org.scalatest.Matchers
import server.MockServerWordSpec

class DiscogsArtistsApiSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient[String] {

  "Discogs OAuth Client" when {

    val client = validOAuthClient

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

}

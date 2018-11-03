package client

import cats.effect.IO
import client.http.IOClient
import entities.PaginatedReleases
import org.scalatest.Matchers
import server.MockServerWordSpec

class DiscogsArtistsApiSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient[String] {

  "Discogs OAuth Client" when {

    val client = validOAuthClient

    "getting Artists releases" should {

      def io: IO[PaginatedReleases] = client.getArtistsReleases(artistId = 1, perPage = 1)

      "be a right" in {
        io.attempt.unsafeRunSync() shouldBe 'right
      }
      "have proper pagination" in {
        io.unsafeRunSync().pagination.page shouldBe 1
      }
      "decode artist object" in {
        io.unsafeRunSync().releases.head.artist shouldBe "Stephan-G* & The Persuader"
      }
    }
  }

}

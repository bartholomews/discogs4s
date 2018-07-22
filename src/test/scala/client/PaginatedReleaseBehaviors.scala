package client

import entities.PaginatedReleases
import org.scalatest.{Matchers, WordSpec}

trait PaginatedReleaseBehaviors extends WordSpec with Matchers {

  import client.DiscogsClientSpec.client

  def paginatedReleasesResponse(request: client.GET[PaginatedReleases])
                               (artistRelease: Int, page: Int, perPage: Int): Unit = {
    "requested as an IO" when behaveLikePaginatedRelease(request.io.unsafeRunSync())
    "requested as an IO[Try]" when behaveLikePaginatedRelease(request.ioTry.unsafeRunSync().get)
    "requested as an IO[Either]" when behaveLikePaginatedRelease(request.ioEither.unsafeRunSync().right.get)

    def behaveLikePaginatedRelease(release: => PaginatedReleases) {
      "have proper pagination" in {
        release.pagination.page shouldBe page
      }
      "decode artist object" in {
        release.releases.head.artist shouldBe "Stephan-G* & The Persuader"
      }
    }
  }
}
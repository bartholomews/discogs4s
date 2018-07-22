package client

import entities.PaginatedReleases
import org.scalatest.{Matchers, WordSpec}

trait PaginatedReleaseBehaviors extends WordSpec with Matchers {

  import client.DiscogsClientSpec.client

  def paginatedReleasesResponse(request: client.GET[PaginatedReleases]): Unit = {
    "as an IO" should behaveLikePaginatedRelease(request.io.unsafeRunSync())
    "as an IO[Try]" should behaveLikePaginatedRelease(request.ioTry.unsafeRunSync().get)
    "as an IO[Either]" should behaveLikePaginatedRelease(request.ioEither.unsafeRunSync().right.get)
  }

  def behaveLikePaginatedRelease(release: => PaginatedReleases): Unit = {
    "have proper pagination" in {
      release.pagination.page shouldBe 2
    }
    "decode artist object" in {
      release.releases.head.artist shouldBe "Stephan-G* & The Persuader"
    }
  }
}
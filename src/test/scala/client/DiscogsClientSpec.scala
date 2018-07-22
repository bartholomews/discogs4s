package client

import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServerWordSpec with Matchers with PaginatedReleaseBehaviors {

    "Discogs Client" when {

      def GET: DiscogsClientSpec.client.GET.type = DiscogsClientSpec.client.GET

      "getting Artists releases" should {
        val getArtistsRelease = GET(ArtistsReleases(1, perPage = 1))
        behave like paginatedReleasesResponse(getArtistsRelease)
      }
    }
}

object DiscogsClientSpec {
  val client: DiscogsClient = DiscogsClient()
}
package client

import client.api.ArtistsReleases
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServerWordSpec with MockClientConfig with Matchers with PaginatedReleaseBehaviors {

  "Discogs OAuth Client" when {

    def GET: DiscogsClientSpec.client.GET.type = DiscogsClientSpec.client.GET

    "getting Artists releases" when {
      parsed like paginatedReleasesResponse {
        GET(ArtistsReleases(1, perPage = 1))
      } (artistRelease = 1, page = 1, perPage = 1)
    }

  }
}

object DiscogsClientSpec extends MockClientConfig {
  val client: DiscogsClient = validOAuthClient
}
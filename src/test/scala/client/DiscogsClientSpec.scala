package client

import cats.effect.IO
import client.api.{ArtistsReleases, AuthorizeUrl}
import entities.ResponseError
import org.http4s.client.oauth1.Consumer
import org.http4s.{Method, Request, Status}
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServerWordSpec with MockClientConfig with Matchers with PaginatedReleaseBehaviors with RequestF[String] {

  "Discogs OAuth Client" when {

    def GET: DiscogsClientSpec.client.GET.type = DiscogsClientSpec.client.GET

    "receiving an unexpected Content-type header while expecting application/json" when {

      implicit val consumer: Consumer = validConsumer

      val requestWithPlainTextResponse: Request[IO] = Request[IO]()
        .withMethod(Method.GET)
        .withUri(AuthorizeUrl.uri)

      val io = fetchJson(requestWithPlainTextResponse).attempt

      "return a ResponseError" should {
        "with UnsupportedMediaType Status and the right error message" in {
          val error = io.unsafeRunSync().left.get.asInstanceOf[ResponseError]
          error.status shouldBe Status.UnsupportedMediaType
          error.getMessage shouldBe
            "text/plain: unexpected Content-Type"
        }
      }
    }

  "getting Artists releases" when {
    parsed like paginatedReleasesResponse {
      GET(ArtistsReleases(1, perPage = 1))
    }(artistRelease = 1, page = 1, perPage = 1)
  }

}

}

object DiscogsClientSpec extends MockClientConfig {
  val client: DiscogsClient = validOAuthClient
}
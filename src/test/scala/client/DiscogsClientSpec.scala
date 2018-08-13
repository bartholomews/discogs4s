package client

import cats.effect.IO
import client.api.{ArtistsReleases, AuthorizeUrl}
import entities.ResponseError
import org.http4s.client.oauth1.Consumer
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.Matchers
import server.MockServerWordSpec
import utils.Config

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with PaginatedReleaseBehaviors
  with IOClient[String] {

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

    "receiving an unexpected json body in the response" when {

      implicit val consumer: Consumer = validConsumer

      val requestWithBadJsonResponse: Request[IO] = Request[IO]()
        .withMethod(Method.GET)
        .withUri(Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}/circe/decoding-error"))

      val io = fetchJson(requestWithBadJsonResponse).attempt

      "return a ResponseError" should {
        "with 500 Status and the right error message" in {
          val error = io.unsafeRunSync().left.get.asInstanceOf[ResponseError]
          error.status shouldBe Status.InternalServerError
          error.getMessage shouldBe
            "There was a problem decoding or parsing this response, please check the error logs."
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
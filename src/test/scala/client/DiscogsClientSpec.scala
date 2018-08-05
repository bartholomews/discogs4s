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

    "receiving an unexpected Content-type header" when {
      "expecting a json response" should {
        "return a ResponseError with 500 Status" in {

          implicit val consumer: Consumer = validConsumer

          val requestWithPlainTextResponse: Request[IO] = Request[IO]()
            .withMethod(Method.GET)
            .withUri(AuthorizeUrl.uri)

          val io = fetchJson(requestWithPlainTextResponse)
            .attempt
            .unsafeRunSync()

          io.isLeft shouldBe true
          val throwable = io.left.get
          throwable.isInstanceOf[ResponseError] shouldBe true
          val responseError: ResponseError = throwable match {
            case e: ResponseError => e
          }
          responseError.status shouldBe Status.InternalServerError
          responseError.getMessage shouldBe
            "There was a problem decoding or parsing this response, please check the error logs."
        }
      }
    }

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
package client

import api.AuthorizeUrl
import cats.effect.IO
import client.http.IOClient
import client.utils.Config
import entities.ResponseError
import org.http4s.client.oauth1.Consumer
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient[String] {

  val client: DiscogsClient = validOAuthClient

  "Discogs Client" when {

    "receiving an unexpected Content-type header while expecting application/json" should {

      implicit val consumer: Consumer = validConsumer

      val requestWithPlainTextResponse: Request[IO] = Request[IO]()
        .withMethod(Method.GET)
        .withUri(AuthorizeUrl.uri)

      val io = fetchJson(requestWithPlainTextResponse)

      "return a ResponseError" should {
        "have UnsupportedMediaType Status and the right error message" in {
          io.unsafeRunSync().entity shouldBe 'left
          val throwable = io.unsafeRunSync().entity.left.get
          throwable.isInstanceOf[ResponseError] shouldBe true
          val error = io.unsafeRunSync().entity.left.get
          error.status shouldBe Status.UnsupportedMediaType
          error.getMessage shouldBe
            "text/plain: unexpected Content-Type"
        }
      }
    }

    "receiving an empty response" should {

      implicit val consumer: Consumer = validConsumer

      val requestWithEmptyResponse: Request[IO] = Request[IO]()
        .withMethod(Method.GET)
        .withUri(Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}/empty-response"))

      val io = fetchJson(requestWithEmptyResponse).attempt

      "return a ResponseError" in {
        val error = io.unsafeRunSync().left.get.asInstanceOf[ResponseError]
        error.status shouldBe Status.BadRequest
        error.getMessage shouldBe
          "Response was empty. Please check request logs."
      }
    }

    "receiving an unexpected json body in the response" should {

      implicit val consumer: Consumer = validConsumer

      val requestWithBadJsonResponse: Request[IO] = Request[IO]()
        .withMethod(Method.GET)
        .withUri(Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}/circe/decoding-error"))

      val io = fetchJson(requestWithBadJsonResponse)

      "return a ResponseError" should {
        "should have 500 Status and the right error message" in {
          val error = io.unsafeRunSync().entity.left.get
          error.status shouldBe Status.InternalServerError
          error.getMessage shouldBe
            "There was a problem decoding or parsing this response, please check the error logs."
        }
      }
    }
  }
}
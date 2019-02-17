package client

import cats.effect.IO
import client.effect4s.IOClient
import client.effect4s.entities.ResponseError
import discogs.utils.Config
import org.http4s.client.oauth1.Consumer
import org.http4s.{Method, Request, Status}
import org.scalatest.Matchers
import server.MockServerWordSpec
import io.circe.Json

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class IOClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient {

  "IOClient" when {

    implicit val consumer: Consumer = validConsumer

    "fetching json" when {

      "receiving a not Ok response with unexpected Content-type header while expecting application/json" should {

        val requestWithHtmlBadRequestResponse: Request[IO] = Request[IO]()
          .withMethod(Method.GET)
          .withUri(Config.discogs.apiUri / unsupportedMediaTypeBadRequestEndpoint)

        val io = fetchJson[Json](requestWithHtmlBadRequestResponse)

        "return a ResponseError" should {
          "have UnsupportedMediaType Status and the right error message" in {
            io.unsafeRunSync().entity shouldBe 'left
            val throwable = io.unsafeRunSync().entity.left.get
            throwable.isInstanceOf[ResponseError] shouldBe true
            val error = io.unsafeRunSync().entity.left.get
            error.status shouldBe Status.UnsupportedMediaType
            error.getMessage shouldBe
              "text/html: unexpected `Content-Type`"
          }
        }
      }

      "receiving a not Ok response without `Content-type header` while expecting application/json" should {

        val requestWithHtmlBadRequestResponse: Request[IO] = Request[IO]()
          .withMethod(Method.GET)
          .withUri(Config.discogs.apiUri / noHeadersBadRequest)

        val io = fetchJson[Json](requestWithHtmlBadRequestResponse)

        "return a ResponseError" should {
          "have UnsupportedMediaType Status and the right error message" in {
            io.unsafeRunSync().entity shouldBe 'left
            val throwable = io.unsafeRunSync().entity.left.get
            throwable.isInstanceOf[ResponseError] shouldBe true
            val error = io.unsafeRunSync().entity.left.get
            error.status shouldBe Status.UnsupportedMediaType
            error.getMessage shouldBe
              "`Content-Type` not provided"
          }
        }
      }

      "receiving an empty response" should {

        implicit val consumer: Consumer = validConsumer

        val requestWithEmptyResponse: Request[IO] = Request[IO]()
          .withMethod(Method.GET)
          .withUri(Config.discogs.apiUri / emptyResponseEndpoint)

        val io = fetchJson[Json](requestWithEmptyResponse)

        "raise an error" in {
          val res = io.unsafeRunSync()
          res.status shouldBe Status.BadRequest
          res.entity.left.get.getMessage shouldBe
            "Response was empty. Please check request logs"
        }
      }

      "receiving a bad status response" should {

        implicit val consumer: Consumer = validConsumer

        val requestWithEmptyResponse: Request[IO] = Request[IO]()
          .withMethod(Method.GET)
          .withUri(Config.discogs.apiUri / notFoundResponseEndpoint)

        val io = fetchJson[Json](requestWithEmptyResponse)

        "return a ResponseError" in {
          val res = io.unsafeRunSync()
          res.status shouldBe Status.NotFound
        }
      }

      "receiving an unexpected json body in the response" should {

        implicit val consumer: Consumer = validConsumer

        val requestWithBadJsonResponse: Request[IO] = Request[IO]()
          .withMethod(Method.GET)
          .withUri(Config.discogs.apiUri / "circe" / "decoding-error")

        val io = fetchJson[Map[String, Boolean]](requestWithBadJsonResponse)

        "return a ResponseError" should {
          "should have 500 Status and the right error message" in {
            val error = io.unsafeRunSync().entity.left.get
            error.status shouldBe Status.InternalServerError
            error.getMessage shouldBe
              "There was a problem decoding or parsing this response, please check the error logs"
          }
        }
      }
    }
  }
}
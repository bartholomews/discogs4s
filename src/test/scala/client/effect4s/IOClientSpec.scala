package client.effect4s

import client.MockClientConfig
import client.discogs.utils.Config
import client.effect4s.config.OAuthConsumer
import client.effect4s.entities.ResponseError
import io.circe.Json
import org.http4s.Status
import org.http4s.client.oauth1.Consumer
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class IOClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers {

  "IOClient" when {

    "has a valid configuration" when {

      val client = new IOClient(OAuthConsumer(
        appName = "app-name",
        appVersion = None,
        appUrl = None,
        key = validConsumerKey,
        secret = validConsumerSecret
      ))

      implicit val consumer: Consumer = validConsumer

      "fetching json" when {

        "receiving a not Ok response with unexpected Content-type header while expecting application/json" should {

          val io = client.fetchJson[Json](Config.discogs.apiUri / unsupportedMediaTypeBadRequestEndpoint)

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

          val io = client.fetchJson[Json](Config.discogs.apiUri / noHeadersBadRequest)

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

          val io = client.fetchJson[Json](Config.discogs.apiUri / emptyResponseEndpoint)

          "raise an error" in {
            val res = io.unsafeRunSync()
            res.status shouldBe Status.BadRequest
            res.entity.left.get.getMessage shouldBe
              "Response was empty. Please check request logs"
          }
        }

        "receiving a bad status response" should {

          implicit val consumer: Consumer = validConsumer

          val io = client.fetchJson[Json](Config.discogs.apiUri / notFoundResponseEndpoint)

          "return a ResponseError" in {
            val res = io.unsafeRunSync()
            res.status shouldBe Status.NotFound
          }
        }

        "receiving an unexpected json body in the response" should {

          implicit val consumer: Consumer = validConsumer

          val io = client.fetchJson[Map[String, Boolean]](Config.discogs.apiUri / "circe" / "decoding-error")

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
}
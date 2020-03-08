package io.bartholomews.discogs4s

import cats.implicits._
import com.github.tomakehurst.wiremock.client.WireMock._
import com.softwaremill.diffx.scalatest.DiffMatcher
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthVersion.V1
import fsclient.entities.{AuthEnabled, HttpResponse}
import io.bartholomews.discogs4s.entities.RequestTokenResponse
import io.bartholomews.discogs4s.wiremock.MockServer
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Status, Uri}
import org.scalatest.{Inside, Matchers}

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsClientSpec extends MockServer with MockClientConfig with Matchers with DiffMatcher with Inside {

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration" should {
      "read the consumer values from resource folder" in {
        val discogs = new DiscogsClient()
        discogs.config.authInfo.signer.consumer.key shouldBe "mock-consumer-key"
        discogs.config.authInfo.signer.consumer.secret shouldBe "mock-consumer-secret"
      }
    }

    "initialised with an explicit configuration" should {

      val config = FsClientConfig(
        userAgent = UserAgent(appName = "mock-app-name", appVersion = None, appUrl = None),
        authInfo = AuthEnabled(V1.BasicSignature(Consumer(key = "consumer-key", secret = "consumer-secret")))
      )

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(config)
        discogs.config.authInfo.signer.consumer.key shouldBe "consumer-key"
        discogs.config.authInfo.signer.consumer.secret shouldBe "consumer-secret"
      }
    }

    "the client has valid consumer keys" when {

      "getting a request token" when {

        "consumer key is invalid" should {
          val client = clientWith("invalidConsumer")

          def response: HttpResponse[RequestTokenResponse] = client.getRequestToken.unsafeRunSync()

          "return a Left with appropriate message" ignore {
            response.entity.leftMap(_.getMessage) shouldBe Left("Invalid consumer.")
          }
        }

        "consumer secret is invalid" should {
          val client = clientWith(consumerWithInvalidSignature, "_")

          def response: HttpResponse[RequestTokenResponse] = client.getRequestToken.unsafeRunSync()

          "return a Left with appropriate message" ignore {
            response.entity.leftMap(_.getMessage) shouldBe
              Left("Invalid signature. Please double check consumer secret key.")
          }
        }

        "consumer key and secret are valid" should {
          val client = validClient

          def response: HttpResponse[RequestTokenResponse] = client.getRequestToken.unsafeRunSync()

          "return a Right with the response Token" ignore {
            response.entity.map(_.token) shouldBe Right(Token("TOKEN", "SECRET"))
          }
          "return a Right with the callback Uri" ignore {
            response.entity.map(_.callback) shouldBe Right(
              Uri.unsafeFromString(
                "http://127.0.0.1:8080/oauth/authorize?oauth_token=TOKEN"
              )
            )
          }
        }

        "getting an unexpected response" should {
          val client = clientWith(consumerGettingUnexpectedResponse, "_")

          def response: HttpResponse[RequestTokenResponse] = client.getRequestToken.unsafeRunSync()

          "return a Left with appropriate message" ignore {
            inside(response) {
              case res @ HttpResponse(_, Left(error)) =>
                res.status shouldBe Status.BadRequest
                error.getMessage shouldBe s"Unexpected response: $unexpectedResponse"
            }
          }
        }

        "custom config has neither consumer application version nor url" should {

          val client = clientWith(appName = "some app", appVersion = None, appUrl = None)

          "have a proper USER-AGENT header" ignore {
            client.getRequestToken
              .unsafeRunSync()
              .headers
              .get(CaseInsensitiveString("User-Agent"))
              .map(_.value) shouldBe Some("some app")
          }
        }
      }

      "getting an auth token" when {

        val client = validClient

        "request is empty" should {

          "return an error with the right code" in {}

          "return an error with the right message" in {}
        }

        "request has an invalid verifier" should {

          "return an error with the right code" in {}

          "return an error with the right message" in {}
        }

        "request is valid" should {

          "return a response with Token" in {

            stubFor(
              get(urlMatching("/oauth/request_token"))
                .willReturn(
                  aResponse()
                    .withStatus(200)
                    .withBody("oauth_token=TK1&oauth_token_secret=fafafafafaffafaffafafa&oauth_callback_confirmed=1")
                )
            )

            inside(client.getRequestToken.unsafeRunSync()) {
              case _ @HttpResponse(_, Right(response)) =>
                response should matchTo(
                  RequestTokenResponse(
                    token = Token("TK1", "fafafafafaffafaffafafa"),
                    callbackConfirmed = true
                  )
                )
            }
          }

          "return a response with the right callback Uri" in {}
        }
      }

      "the client has invalid consumer keys" when {

        val client: DiscogsClient = clientWith("invalid-key")

        "getting a request token" should {
          "fail" ignore {
            val res = client.getRequestToken.unsafeRunSync()
            res.status shouldBe Status.Unauthorized
          }
        }

        "getting a oAuth client" should {
          "fail" ignore {}
        }
      }
    }
  }

}

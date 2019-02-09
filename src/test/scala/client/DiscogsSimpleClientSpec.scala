package client

import api.AccessTokenRequest
import client.http.IOClient
import client.utils.Config.DiscogsConsumer
import org.http4s.Status
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsSimpleClientSpec extends MockServerWordSpec
  with MockClientConfig
  with Matchers
  with IOClient[String] {

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration" should {
      "read the consumer values from resource folder" in {
        val client = new DiscogsSimpleClient()
        client.consumer.key shouldBe "mock-consumer-key"
        client.consumer.secret shouldBe "mock-consumer-secret"
      }
    }

    "initialised with an explicit configuration" should {

      val config = DiscogsConsumer(
        appName = "mock-app-name",
        appVersion = None,
        appUrl = None,
        key = "consumer-key",
        secret = "consumer-secret"
      )

      "read the consumer values from the injected configuration" in {
        val client = new DiscogsSimpleClient(config)
        client.consumer.key shouldBe "consumer-key"
        client.consumer.secret shouldBe "consumer-secret"
      }
    }

    "the client has valid consumer keys" when {

      val client: DiscogsSimpleClient = validClient

      "get a request token" should {
        "succeed" in {
          val res = client.RequestToken.get.unsafeRunSync()
          res.status shouldBe Status.Ok
        }
      }

      "get a oAuth client" should {

        "succeed" in {
          val oAuthClient = for {
            requestToken <- client.RequestToken.get.unsafeRunSync().entity
            res <- client.getOAuthClient(AccessTokenRequest(requestToken.token, validVerifier)).unsafeRunSync()
          } yield res

          oAuthClient shouldBe 'right
          oAuthClient.right.get.consumer shouldBe client.consumer
        }
      }
    }

    "the client has invalid consumer keys" when {

      val client: DiscogsSimpleClient = clientWith("invalid-key")

      "get a request token" should {
        "fail" in {
          val res = client.RequestToken.get.unsafeRunSync()
          res.status shouldBe Status.BadRequest
        }
      }

      "get a oAuth client" should {
        "fail" in {
          val oAuthClient = for {
            requestToken <- client.RequestToken.get.unsafeRunSync().entity
            res <- client.getOAuthClient(AccessTokenRequest(requestToken.token, validVerifier)).unsafeRunSync()
          } yield res
          oAuthClient shouldBe 'left
        }
      }
    }

  }

}
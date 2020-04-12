package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.client.ClientData
import io.bartholomews.fsclient.config.{FsClientConfig, UserAgent}
import io.bartholomews.fsclient.entities.OAuthVersion.Version1.{AccessTokenV1, BasicSignature}
import io.bartholomews.fsclient.entities.{OAuthEnabled, SignerV1}
import io.bartholomews.fsclient.requests.OAuthV1AuthorizationFramework.{OAuthV1AccessToken, OAuthV1BasicSignature}
import io.bartholomews.testudo.WireWordSpec
import org.http4s.client.oauth1.{Consumer, Token}

class DiscogsClientSpec extends WireWordSpec {

  import ClientData._

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration for `BasicSignature`" should {
      "read the consumer values from resource folder" in {
        val discogs = DiscogsClient.unsafeFromConfig(OAuthV1BasicSignature)
        inside(discogs.appConfig.authInfo) {
          case OAuthEnabled(signer: SignerV1) =>
            signer.consumer.key should matchTo("mock-consumer-key")
            signer.consumer.secret should matchTo("mock-consumer-secret")
        }
      }
    }

    "initialised with an implicit configuration for `AccessToken`" should {
      "read the consumer values from resource folder" in {
        val discogs = DiscogsClient.unsafeFromConfig(OAuthV1AccessToken)
        inside(discogs.appConfig) {
          case FsClientConfig(userAgent, OAuthEnabled(AccessTokenV1(token, verifier, consumer))) =>
            token should matchTo(Token("TOKEN_VALUE", "TOKEN_SECRET"))
            verifier.isEmpty shouldBe true
            consumer.key should matchTo("mock-consumer-key")
            consumer.secret should matchTo("mock-consumer-secret")
        }
      }
    }

    "initialised with an explicit `BasicSignature` configuration" should {

      val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)
      val basicSignature = BasicSignature(sampleConsumer)

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(sampleUserAgent, basicSignature)
        inside(discogs.appConfig) {
          case FsClientConfig(userAgent, OAuthEnabled(signer: SignerV1)) =>
            userAgent should matchTo(sampleUserAgent)
            signer.consumer should matchTo(
              Consumer(key = sampleConsumer.key, secret = sampleConsumer.secret)
            )
        }
      }
    }
  }
}

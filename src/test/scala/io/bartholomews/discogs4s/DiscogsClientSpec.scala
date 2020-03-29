package io.bartholomews.discogs4s

import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.OAuthVersion.V1
import fsclient.entities.OAuthVersion.V1.AccessToken
import fsclient.entities.{OAuthEnabled, SignerType}
import org.http4s.client.oauth1.{Consumer, Token}

class DiscogsClientSpec extends StubbedWordSpec {

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration for `BasicSignature`" should {
      "read the consumer values from resource folder" in {
        val discogs = DiscogsClient.unsafeFromConfig(SignerType.OAuthV1BasicSignature)
        discogs.appConfig.authInfo.signer.consumer.key should matchTo("mock-consumer-key")
        discogs.appConfig.authInfo.signer.consumer.secret should matchTo("mock-consumer-secret")
      }
    }

    "initialised with an implicit configuration for `AccessToken`" should {
      "read the consumer values from resource folder" in {
        val discogs = DiscogsClient.unsafeFromConfig(SignerType.OAuthV1AccessToken)
        inside(discogs.appConfig) {
          case FsClientConfig(userAgent, OAuthEnabled(AccessToken(token, verifier, consumer))) =>
            token should matchTo(Token("TOKEN_VALUE", "TOKEN_SECRET"))
            verifier shouldBe empty
            consumer.key should matchTo("mock-consumer-key")
            consumer.secret should matchTo("mock-consumer-secret")
        }
      }
    }

    "initialised with an explicit `BasicSignature` configuration" should {

      val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)
      val basicSignature = V1.BasicSignature(sampleConsumer)

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(sampleUserAgent, basicSignature)
        discogs.appConfig.userAgent should matchTo(sampleUserAgent)
        discogs.appConfig.authInfo.signer.consumer should matchTo(
          Consumer(key = sampleConsumer.key, secret = sampleConsumer.secret)
        )
      }
    }
  }
}

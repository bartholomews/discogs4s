package io.bartholomews.discogs4s

import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.OAuthEnabled
import fsclient.entities.OAuthVersion.V1
import org.http4s.client.oauth1.Consumer

class DiscogsClientSpec extends StubbedWordSpec {

  "DiscogsSimpleClient" when {

    // TODO: Should probably verify that client constructed is a `BasicSignatureClient`

    "initialised with an implicit configuration" should {
      "read the consumer values from resource folder" in {
        val discogs = new DiscogsClient()
        discogs.config.authInfo.signer.consumer.key shouldBe "mock-consumer-key"
        discogs.config.authInfo.signer.consumer.secret shouldBe "mock-consumer-secret"
      }
    }

    "initialised with an explicit configuration" should {

      val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)

      val config = FsClientConfig(
        userAgent = sampleUserAgent,
        authInfo = OAuthEnabled(V1.BasicSignature(sampleConsumer))
      )

      "read the consumer values from the injected configuration" in {
        val discogs = new DiscogsClient(config)
        discogs.config.userAgent shouldBe sampleUserAgent
        discogs.config.authInfo.signer.consumer shouldBe Consumer(
          key = sampleConsumer.key,
          secret = sampleConsumer.secret
        )
      }
    }
  }
}

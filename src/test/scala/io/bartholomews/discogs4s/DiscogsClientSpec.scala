package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.client.ClientData
import io.bartholomews.fsclient.config.UserAgent
import io.bartholomews.fsclient.entities.oauth.ClientCredentials
import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
import io.bartholomews.scalatestudo.WireWordSpec

class DiscogsClientSpec extends WireWordSpec {

  import ClientData._

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration for `BasicSignature`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.unsafeFromConfig(SignerType.BasicSignature)
        }
      }
    }

    "initialised with an implicit configuration for `AccessToken`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.unsafeFromConfig(SignerType.TokenSignature)
        }
      }

      "initialised with an explicit `BasicSignature` configuration" should {

        val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)
        val basicSignature = ClientCredentials(sampleConsumer)

        "read the consumer values from the injected configuration" in {
          noException should be thrownBy {
            new DiscogsClient(sampleUserAgent, basicSignature)
          }
        }
      }
    }
  }
}

package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.client.ClientData
import io.bartholomews.fsclient.core.config.UserAgent
import sttp.client.Identity

class DiscogsClientSpec extends CoreWireWordSpec {

  import ClientData._

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration for `BasicSignature`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.clientCredentialsFromConfig[Identity]
        }
      }
    }

    "initialised with an implicit configuration for `AccessToken`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.personalFromConfig[Identity]
        }
      }

      "initialised with an explicit `BasicSignature` configuration" should {

        val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)
        val basicSignature = sampleConsumer

        "read the consumer values from the injected configuration" in {
          noException should be thrownBy {
            DiscogsClient.clientCredentials(sampleUserAgent, basicSignature)
          }
        }
      }
    }
  }
}

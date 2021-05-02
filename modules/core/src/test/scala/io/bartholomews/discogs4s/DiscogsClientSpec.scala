package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.scalatestudo.data.ClientData.v1.sampleConsumer
import sttp.client3.Identity

class DiscogsClientSpec extends DiscogsWireWordSpec {

  import DiscogsClientData._

  "DiscogsSimpleClient" when {

    "initialised with an implicit configuration for `BasicSignature`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.clientCredentials.unsafeFromConfig[Identity](backend)
        }
      }
    }

    "initialised with an implicit configuration for `AccessToken`" should {
      "read the consumer values from resource folder" in {
        noException should be thrownBy {
          DiscogsClient.personal.unsafeFromConfig[Identity](backend)
        }
      }

      "initialised with an explicit `BasicSignature` configuration" should {

        val sampleUserAgent = UserAgent(appName = "mock-app-name", appVersion = Some("1.0"), appUrl = None)
        val basicSignature = sampleConsumer

        "read the consumer values from the injected configuration" in {
          noException should be thrownBy {
            DiscogsClient.clientCredentials(sampleUserAgent, basicSignature)(backend)
          }
        }
      }
    }
  }
}

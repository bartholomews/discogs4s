package io.bartholomews.discogs4s.client

import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.{ClientCredentials, RedirectUri}
import io.bartholomews.scalatestudo.data.ClientData.v1.sampleConsumer
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext}

object DiscogsClientData {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  val sampleRedirectUri: RedirectUri =
    RedirectUri(uri"https://bartholomews.io/discogs4s/callback")

  val sampleClient =
    new DiscogsClient[Identity, ClientCredentials](
      UserAgent("discogs-test", appVersion = None, appUrl = None),
      ClientCredentials(sampleConsumer)
    )(backend)

  case class DiscogsError(message: String)
}

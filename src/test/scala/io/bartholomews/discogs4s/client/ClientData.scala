package io.bartholomews.discogs4s.client

import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.ClientCredentials
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, Token}
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RedirectUri
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext}

object ClientData {

  val sampleConsumer: Consumer = Consumer(
    key = "SAMPLE_CONSUMER_KEY",
    secret = "SAMPLE_CONSUMER_SECRET"
  )

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  val sampleToken: Token = Token(value = "SAMPLE_TOKEN_VALUE", secret = "SAMPLE_TOKEN_SECRET")

  val sampleRedirectUri: RedirectUri =
    RedirectUri(uri"https://bartholomews.io/discogs4s/callback")

  val sampleClient =
    new DiscogsClient[Identity, ClientCredentials](
      UserAgent("discogs-test", appVersion = None, appUrl = None),
      ClientCredentials(sampleConsumer)
    )(backend)

  case class DiscogsError(message: String)
}

package io.bartholomews.discogs4s.client

import io.bartholomews.discogs4s.{DiscogsClient, DiscogsOAuthClient}
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, ClientCredentials, RedirectUri}
import io.bartholomews.scalatestudo.data.ClientData.v1.sampleConsumer
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext}

object DiscogsClientData {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  val sampleRedirectUri: RedirectUri =
    RedirectUri(uri"https://bartholomews.io/discogs4s/callback")

  val accessTokenCredentials: AccessTokenCredentials = AccessTokenCredentials(
    Token("TOKEN_VALUE", "TOKEN_SECRET"),
    sampleConsumer,
    SignatureMethod.SHA1
  )

  val clientCredentials: ClientCredentials = ClientCredentials(sampleConsumer)

  val sampleOAuthClient: DiscogsOAuthClient[Identity] =
    DiscogsClient.oAuth.apply(
      UserAgent("discogs-test", appVersion = None, appUrl = None),
      sampleConsumer
    )(backend)

  case class DiscogsError(message: String)
}

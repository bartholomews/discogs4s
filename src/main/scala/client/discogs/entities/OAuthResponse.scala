package client.discogs.entities

import client.discogs.api.AuthorizeUrl
import client.effect4s.entities.OAuthAccessToken
import client.discogs.utils.Config
import org.http4s.Uri
import org.http4s.client.oauth1.Token

case class RequestTokenResponse(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = (Config.discogs.baseUri / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

case class AccessTokenResponse(token: Token) extends OAuthAccessToken

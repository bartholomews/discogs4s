package entities

import api.{AuthorizeUrl, OAuthAccessToken}
import client.utils.Config
import org.http4s.Uri
import org.http4s.client.oauth1.Token

case class RequestTokenResponse(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = (Config.discogs.baseUri / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

case class AccessTokenResponse(token: Token) extends OAuthAccessToken

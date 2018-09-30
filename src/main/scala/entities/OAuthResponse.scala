package entities

import api.AuthorizeUrl
import org.http4s.Uri
import org.http4s.client.oauth1.Token
import client.utils.Config

trait OAuthResponse {
  val token: Token
}

case class RequestTokenResponse(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = (Uri.unsafeFromString(
    s"${Config.SCHEME}://${Config.DISCOGS_DOMAIN}") / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

case class AccessTokenResponse(token: Token) extends OAuthResponse

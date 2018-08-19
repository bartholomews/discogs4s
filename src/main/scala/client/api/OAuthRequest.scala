package client.api

import org.http4s.Uri
import org.http4s.client.oauth1.Token
import utils.Config

sealed trait OAuthRequest[T] extends DiscogsApi[T] {
  private[client] val path: String = "oauth"
  private[client] val basePath: Uri = baseUrl / path
}

case object AuthorizeUrl extends OAuthRequest[Uri] {
  override val uri: Uri = basePath / "request_token"
  def response(token: String): Uri =
    (Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_DOMAIN}") / path / "authorize")
      .withQueryParam("oauth_token", token)
}

case class OAuthResponse(token: Token) {
  val callback: Uri = (Uri.unsafeFromString(
    s"${Config.SCHEME}://${Config.DISCOGS_DOMAIN}") / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

case class AccessTokenRequest(token: Token, verifier: String) extends OAuthRequest[Token] {
  override val uri: Uri = basePath / "access_token"
}

case object Identity extends OAuthRequest[String] { // FIXME Identity proper entity json type
  override val uri: Uri = basePath / "identity"
}
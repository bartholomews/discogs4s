package client.api

import org.http4s.Uri
import utils.Config

sealed trait OAuthRequest extends DiscogsApi[String] {
  private[client] val path: String = "oauth"
  private[client] val basePath: Uri = baseUrl / path
}

case object AuthorizeUrl extends OAuthRequest {
  override val uri: Uri = basePath / "request_token"
  def response(token: String): Uri =
    (Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_DOMAIN}") / path / "authorize")
      .withQueryParam("oauth_token", token)
}
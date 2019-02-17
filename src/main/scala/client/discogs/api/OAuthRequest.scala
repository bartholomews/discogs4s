package client.discogs.api

import client.effect4s.entities.OAuthAccessToken
import org.http4s.Uri
import org.http4s.client.oauth1.Token

sealed trait OAuthRequest[T] extends DiscogsApi[T] {
  val path: String = "oauth"
  private[api] val basePath: Uri = apiUri / path
}

case object AuthorizeUrl extends OAuthRequest[Uri] {
  override val uri: Uri = basePath / "request_token"
}

case class AccessTokenRequest(token: Token, oAuthVerifier: String) extends OAuthRequest[Token] with OAuthAccessToken {
  override val verifier = Some(oAuthVerifier)
  override val uri: Uri = basePath / "access_token"
}

case object Identity extends OAuthRequest[String] {
  // TODO and FIXME => Identity proper entity json type
  override val uri: Uri = basePath / "identity"
}
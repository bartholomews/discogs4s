package api

import org.http4s.Uri
import org.http4s.client.oauth1.Token
import client.utils.Config

sealed trait OAuthRequest[T] extends DiscogsApi[T] {
  val path: String = "oauth"
  private[api] val basePath: Uri = baseUrl / path
}

case object AuthorizeUrl extends OAuthRequest[Uri] {
  override val uri: Uri = basePath / "request_token"
}

case class AccessTokenRequest(token: Token, verifier: String) extends OAuthRequest[Token] {
  override val uri: Uri = basePath / "access_token"
}

//case object Identity extends OAuthRequest[String] { // TODO and FIXME => Identity proper entity json type
//  override val uri: Uri = basePath / "identity"
//}
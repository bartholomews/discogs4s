package client.discogs.api

import client.discogs.entities.UserIdentity
import client.effect4s.entities.OAuthAccessToken
import org.http4s.Uri
import org.http4s.client.oauth1.Token

sealed trait OAuthApi[T] extends DiscogsEndpoint[T] {
  val path: String = "oauth"
  private[api] val basePath: Uri = apiUri / path
}

case object AuthorizeUrl extends OAuthApi[Uri] with HttpMethod.GET {
  override val uri: Uri = basePath / "request_token"
}

case class AccessTokenApi(token: Token, oAuthVerifier: String)
  extends OAuthApi[Token] with OAuthAccessToken with HttpMethod.POST {

  override val verifier = Some(oAuthVerifier)
  override val uri: Uri = basePath / "access_token"
}

case object Identity extends OAuthApi[UserIdentity] with HttpMethod.GET {
  override val uri: Uri = basePath / "identity"
}
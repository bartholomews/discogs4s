package io.bartholomews.discogs4s.endpoints

import io.bartholomews.fsclient.requests.{AccessTokenEndpointBase, AuthJsonRequest, PlainTextRequest}
import io.bartholomews.discogs4s.entities.{RequestToken, UserIdentity}
import org.http4s.Uri

// https://www.discogs.com/developers#page:authentication,header:authentication-request-token-url
sealed trait OAuthEndpoint extends DiscogsEndpoint {
  final val path: String = "oauth"
  private[endpoints] val basePath: Uri = apiUri / path
}

case object AuthorizeUrl extends OAuthEndpoint with PlainTextRequest.Get[RequestToken] {
  override val uri: Uri = basePath / "request_token"
}

case object AccessTokenEndpoint extends OAuthEndpoint with AccessTokenEndpointBase {
  override val uri: Uri = basePath / "access_token"
}

case object Identity extends AuthJsonRequest.Get[UserIdentity] with OAuthEndpoint {
  override val uri: Uri = basePath / "identity"
}

package io.bartholomews.discogs4s.api

import fsclient.requests.{AccessTokenEndpointBase, AuthJsonRequest, PlainTextRequest}
import io.bartholomews.discogs4s.entities.{RequestTokenResponse, UserIdentity}
import org.http4s.Uri

// https://www.discogs.com/developers#page:authentication,header:authentication-request-token-url
sealed trait OAuthApi extends DiscogsEndpoint {
  val path: String = "oauth"
  private[api] val basePath: Uri = apiUri / path
}

case object AuthorizeUrl extends OAuthApi with PlainTextRequest.Get[RequestTokenResponse] {
  override val uri: Uri = basePath / "request_token"
}

case object AccessTokenEndpoint extends OAuthApi with AccessTokenEndpointBase {
  val uri: Uri = basePath / "access_token"
}

case object Identity extends AuthJsonRequest.Get[UserIdentity] with OAuthApi {
  override val uri: Uri = basePath / "identity"
}
package io.bartholomews.discogs4s.endpoints

import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint._
import io.bartholomews.discogs4s.entities.{RequestToken, UserIdentity}
import io.bartholomews.fsclient.requests.{AccessTokenEndpointBase, AuthJsonRequest, AuthPlainTextRequest}
import org.http4s.Uri

// https://www.discogs.com/developers#page:authentication,header:authentication-request-token-url
sealed trait DiscogsAuthEndpoint
object DiscogsAuthEndpoint {
  final val path: String = "oauth"
  final val basePath: Uri = DiscogsEndpoint.apiUri / DiscogsAuthEndpoint.path
  final val authorizeUri = DiscogsEndpoint.baseUri / path / "authorize"
  final val revokeUri = DiscogsEndpoint.baseUri / path / "revoke"
}

case object AuthorizeUrl extends DiscogsAuthEndpoint with AuthPlainTextRequest.Get[RequestToken] {
  override val uri: Uri = basePath / "request_token"
}

case object AccessTokenEndpoint extends DiscogsAuthEndpoint with AccessTokenEndpointBase {
  override val uri: Uri = basePath / "access_token"
}

case object Identity extends AuthJsonRequest.Get[UserIdentity] with DiscogsAuthEndpoint {
  override val uri: Uri = basePath / "identity"
}

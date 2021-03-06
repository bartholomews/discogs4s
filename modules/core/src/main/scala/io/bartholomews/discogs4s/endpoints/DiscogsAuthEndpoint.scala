package io.bartholomews.discogs4s.endpoints

import io.bartholomews.fsclient.core.http.FsClientSttpExtensions.UriExtensions
import io.bartholomews.fsclient.core.oauth.TokenCredentials
import sttp.model.Uri

// https://www.discogs.com/developers#page:authentication,header:authentication-request-token-url
sealed trait DiscogsAuthEndpoint
object DiscogsAuthEndpoint {
  final val path: String  = "oauth"
  final val basePath: Uri = DiscogsEndpoint.apiUri / DiscogsAuthEndpoint.path
  final val authorizeUri  = DiscogsEndpoint.baseUri / path / "authorize"
  /*
    Unfortunately discogs support for stateless apps / repeated authorization is very poor;
    if you don't want to implement your own login,
    the user needs to request permissions over and over to regenerate cleared tokens,
    and the older (same) app won't be cleared from the user apps list.
    It's probably good practice at least to redirect the user to revoke the app,
    otherwise it will clutter its apps lists after few usages.
    See https://www.discogs.com/forum/thread/753167
   */
  final def revokeUri(credentials: TokenCredentials): Uri =
    (DiscogsEndpoint.baseUri / path / "revoke")
      .withQueryParam("access_key", credentials.token.value)
}

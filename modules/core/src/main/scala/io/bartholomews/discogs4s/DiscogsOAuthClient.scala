package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.Consumer
import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, RedirectUri, TemporaryCredentialsRequest}
import sttp.client3.SttpBackend

// Client for `oAuth` option;
class DiscogsOAuthClient[F[_]](userAgent: UserAgent, consumer: Consumer)(
  backend: SttpBackend[F, Any]
) {

  def temporaryCredentialsRequest(redirectUri: RedirectUri): TemporaryCredentialsRequest =
    TemporaryCredentialsRequest(consumer, redirectUri)

  final val auth: AuthApi[F] = new AuthApi[F](userAgent, backend)
  final val artists: ArtistsApi[F, AccessTokenCredentials] =
    new ArtistsApi[F, AccessTokenCredentials](userAgent, backend)

  final val users: UsersApi[F, AccessTokenCredentials] =
    new UsersApi[F, AccessTokenCredentials](userAgent, backend)
}

package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{DatabaseApi, AuthApi, UsersApi}
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.Consumer
import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, RedirectUri, TemporaryCredentialsRequest}
import sttp.client3.SttpBackend

// Client for `oAuth` option;
class DiscogsOAuthClient[F[_]] private[discogs4s] (userAgent: UserAgent, consumer: Consumer)(
  backend: SttpBackend[F, Any]
) {

  def temporaryCredentialsRequest(redirectUri: RedirectUri): TemporaryCredentialsRequest =
    TemporaryCredentialsRequest(consumer, redirectUri)

  final val auth: AuthApi[F] = new AuthApi[F](userAgent, backend)
  final val database: DatabaseApi[F, AccessTokenCredentials] =
    new DatabaseApi[F, AccessTokenCredentials](userAgent, backend)

  final val users: UsersApi[F] =
    new UsersApi[F](userAgent, backend)
}

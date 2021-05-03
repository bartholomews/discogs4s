package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{ArtistsApi, UsersApi}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.OAuthSigner

// Client for `personal` option;
// The api should be the same as `oAuth` option, but with internal signer.
class DiscogsPersonalClient[F[_], S <: OAuthSigner](client: FsClient[F, S]) {

  final object artists {
    private val api = new ArtistsApi[F, S](client.userAgent, client.backend)
    def getArtistsReleases[DE](artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])(
      implicit responseHandler: ResponseHandler[DE, PaginatedReleases]
    ): F[SttpResponse[DE, PaginatedReleases]] =
      api.getArtistsReleases(artistId, sortBy, sortOrder)(client.signer)
  }

  final object users {
    private val api = new UsersApi[F, S](client.userAgent, client.backend)
    def getSimpleUserProfile[DE](
      username: Username
    )(implicit responseHandler: ResponseHandler[DE, SimpleUser]): F[SttpResponse[DE, SimpleUser]] =
      api.getSimpleUserProfile(username)(client.signer)

    def me[DE](
      implicit
      responseHandler: ResponseHandler[DE, UserIdentity]
    ): F[SttpResponse[DE, UserIdentity]] = api.me(client.signer)
  }
}

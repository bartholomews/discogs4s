package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{DatabaseApi, UsersApi}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.Signer

// Client for both `authDisabled` and `clientCredentials` options;
// the api should be the same, the only difference is the Consumer key/secret which can be optional
class DiscogsSimpleClient[F[_], S <: Signer] private (client: FsClient[F, S]) {

  final object database {
    private val api = new DatabaseApi[F, S](client.userAgent, client.backend)
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
  }
}

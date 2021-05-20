package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{DatabaseApi, UsersApi}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.OAuthSigner
import sttp.client3.BodySerializer

// Client for `personal` option;
// The api should be the same as `oAuth` option, but with internal signer.
class DiscogsPersonalClient[F[_], S <: OAuthSigner] private[discogs4s] (client: FsClient[F, S]) {

  final object database {
    private val api = new DatabaseApi[F](client.userAgent, client.backend)

    def getRelease[DE](releaseId: DiscogsReleaseId, marketplaceCurrency: Option[MarketplaceCurrency] = None)(implicit
        responseHandler: ResponseHandler[DE, Release]
    ): F[SttpResponse[DE, Release]] =
      api.getRelease(releaseId, marketplaceCurrency)(client.signer)

    def getReleaseRating[DE](releaseId: DiscogsReleaseId, username: DiscogsUsername)(implicit
        responseHandler: ResponseHandler[DE, ReleaseRating]
    ): F[SttpResponse[DE, ReleaseRating]] =
      api.getReleaseRating(releaseId, username)(client.signer)

    def getArtistsReleases[DE](artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])(implicit
        responseHandler: ResponseHandler[DE, PaginatedReleases]
    ): F[SttpResponse[DE, PaginatedReleases]] =
      api.getArtistReleases(artistId, sortBy, sortOrder)(client.signer)
  }

  final object users {
    private val api = new UsersApi[F](client.userAgent, client.backend)

    def me[DE](implicit
        responseHandler: ResponseHandler[DE, UserIdentity]
    ): F[SttpResponse[DE, UserIdentity]] = api.me(client.signer)

    def getUserProfile[DE](
        username: DiscogsUsername
    )(implicit responseHandler: ResponseHandler[DE, UserProfile]): F[SttpResponse[DE, UserProfile]] =
      api.getUserProfile(username)(client.signer)

    def updateUserProfile[DE](username: DiscogsUsername, request: UpdateUserRequest)(implicit
        bodySerializer: BodySerializer[UpdateUserRequest],
        responseHandler: ResponseHandler[DE, UserProfile]
    ): F[SttpResponse[DE, UserProfile]] = api.updateUserProfile(username, request)(client.signer)

    def getUserSubmissions[DE](username: DiscogsUsername, page: Int = 1, perPage: Int = 50)(implicit
        responseHandler: ResponseHandler[DE, UserSubmissionResponse]
    ): F[SttpResponse[DE, UserSubmissionResponse]] = api.getUserSubmissions(username, page, perPage)(client.signer)

    def getUserContributions[DE](
        username: DiscogsUsername,
        page: Int = 1,
        perPage: Int = 50,
        sortBy: Option[UserContributions.SortedBy],
        sortOrder: Option[SortOrder]
    )(implicit
        responseHandler: ResponseHandler[DE, UserContributions]
    ): F[SttpResponse[DE, UserContributions]] = api.getUserContributions(
      username,
      page,
      perPage,
      sortBy,
      sortOrder
    )(client.signer)
  }
}

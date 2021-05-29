package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{DatabaseApi, UsersApi}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.Signer

// Client for both `authDisabled` and `clientCredentials` options;
// the api should be the same, the only difference is the Consumer key/secret which can be optional
class DiscogsSimpleClient[F[_], S <: Signer] private[discogs4s] (client: FsClient[F, S]) {

  final object database {
    private val api = new DatabaseApi[F](client.userAgent, client.backend)

    def getRelease[DE](releaseId: DiscogsReleaseId, marketplaceCurrency: Option[MarketplaceCurrency] = None)(implicit
        responseHandler: ResponseHandler[DE, Release]
    ): F[SttpResponse[DE, Release]] =
      api.getRelease(releaseId, marketplaceCurrency)(client.signer)

    def getReleaseRating[DE](username: DiscogsUsername, releaseId: DiscogsReleaseId)(implicit
        responseHandler: ResponseHandler[DE, ReleaseRating]
    ): F[SttpResponse[DE, ReleaseRating]] =
      api.getReleaseRating(username, releaseId)(client.signer)

    def getCommunityReleaseRating[DE](releaseId: DiscogsReleaseId)(implicit
        responseHandler: ResponseHandler[DE, CommunityRelease]
    ): F[SttpResponse[DE, CommunityRelease]] =
      api.getCommunityReleaseRating(releaseId)(client.signer)

    def getReleaseStats[DE](releaseId: DiscogsReleaseId)(implicit
        responseHandler: ResponseHandler[DE, CommunityReleaseStats]
    ): F[SttpResponse[DE, CommunityReleaseStats]] =
      api.getReleaseStats(releaseId)(client.signer)

    def getMasterRelease[DE](masterId: MasterId)(implicit
        responseHandler: ResponseHandler[DE, MasterRelease]
    ): F[SttpResponse[DE, MasterRelease]] =
      api.getMasterRelease(masterId)(client.signer)

    def getMasterReleaseVersions[DE](masterId: MasterId)(implicit
        responseHandler: ResponseHandler[DE, MasterReleaseVersions]
    ): F[SttpResponse[DE, MasterReleaseVersions]] =
      api.getMasterReleaseVersions(masterId)(client.signer)

    def getArtistsReleases[DE](artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])(implicit
        responseHandler: ResponseHandler[DE, PaginatedReleases]
    ): F[SttpResponse[DE, PaginatedReleases]] =
      api.getArtistReleases(artistId, sortBy, sortOrder)(client.signer)
  }

  final object users {
    private val api = new UsersApi[F](client.userAgent, client.backend)

    def getUserProfile[DE](
        username: DiscogsUsername
    )(implicit responseHandler: ResponseHandler[DE, UserProfile]): F[SttpResponse[DE, UserProfile]] =
      api.getUserProfile(username)(client.signer)

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

package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.DiscogsEndpoint
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.Signer
import sttp.client3.{BodySerializer, SttpBackend}
import sttp.model.Uri

/**
 * https://www.discogs.com/developers/#page:database
 * @param userAgent
 *   the application `User-Agent`, which will be added as header in all the requests
 * @param backend
 *   the Sttp backend for the requests
 * @tparam F
 *   the Effect type
 */
class DatabaseApi[F[_]](userAgent: UserAgent, backend: SttpBackend[F, Any]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  final val artistsPath  = DiscogsEndpoint.apiUri / "artists"
  final val releasesPath = DiscogsEndpoint.apiUri / "releases"

  /**
   * https://www.discogs.com/developers/#page:database,header:database-release
   *
   * The Release resource represents a particular physical or digital object released by one or more Artists.
   *
   * @param releaseId
   *   The Release ID
   * @param marketplaceCurrency
   *   Currency for marketplace data. Defaults to the authenticated users currency.
   * @param signer
   *   the request Signer
   * @param responseHandler
   *   the response decoder
   * @tparam DE
   *   the Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, Release]]`
   */
  def getRelease[DE](releaseId: DiscogsReleaseId, marketplaceCurrency: Option[MarketplaceCurrency] = None)(
      signer: Signer
  )(implicit
      responseHandler: ResponseHandler[DE, Release]
  ): F[SttpResponse[DE, Release]] = {
    val uri: Uri =
      (releasesPath / releaseId.value.toString)
        .withOptionQueryParam("curr_abbr", marketplaceCurrency.map(_.entryName))

    baseRequest(userAgent)
      .get(uri)
      .sign(signer)
      .response(responseHandler)
      .send(backend)
  }

  /**
   * https://www.discogs.com/developers/#page:database,header:database-release-rating-by-user-get
   *
   * Retrieves the release’s rating for a given user.
   *
   * @param username
   *   The username of the rating you are trying to request.
   * @param releaseId
   *   The Release ID
   * @param signer
   *   the request Signer
   * @param responseHandler
   *   the response decoder
   * @tparam DE
   *   the Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, ReleaseRating]]`
   */
  def getReleaseRating[DE](username: DiscogsUsername, releaseId: DiscogsReleaseId)(
      signer: Signer
  )(implicit responseHandler: ResponseHandler[DE, ReleaseRating]): F[SttpResponse[DE, ReleaseRating]] =
    baseRequest(userAgent)
      .get(releasesPath / releaseId.value.toString / "rating" / username.value)
      .sign(signer)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:database,header:database-release-rating-by-user-put
   *
   * Updates the release’s rating for a given user. Authentication as the user is required.
   *
   * @param request
   *   The username of the rating you are trying to request The Release ID The new rating for a release between 1 and 5
   * @param signer
   *   the request Signer
   * @param responseHandler
   *   the response decoder
   * @tparam DE
   *   the Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, ReleaseRating]]`
   */
  def updateReleaseRating[DE](request: ReleaseRatingUpdateRequest)(
      signer: Signer
  )(implicit
      bodySerializer: BodySerializer[ReleaseRatingUpdateRequest],
      responseHandler: ResponseHandler[DE, ReleaseRating]
  ): F[SttpResponse[DE, ReleaseRating]] =
    baseRequest(userAgent)
      .put(releasesPath / request.releaseId.value.toString / "rating" / request.username.value)
      .sign(signer)
      .body(request)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:database,header:database-release-rating-by-user-delete
   *
   * Deletes the release’s rating for a given user. Authentication as the user is required.
   *
   * @param username
   *   The username of the rating you are trying to request.
   * @param releaseId
   *   The Release ID
   * @param signer
   *   the request Signer
   * @return
   *   `F[SttpResponse[DE, Unit]]`
   */
  def deleteReleaseRating(username: DiscogsUsername, releaseId: DiscogsReleaseId)(
      signer: Signer
  ): F[SttpResponse[Nothing, Unit]] =
    baseRequest(userAgent)
      .delete(releasesPath / releaseId.value.toString / "rating" / username.value)
      .sign(signer)
      .response(asUnit)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:database,header:database-artist-releases
   *
   * Get an artist’s releases
   *
   * @param artistId
   *   The Artist ID
   * @param sortBy
   *   Sort items by this field: year (i.e. year of the release) title (i.e. title of the release) format
   * @param sortOrder
   *   Sort items in a particular order (one of asc, desc)
   * @param signer
   *   the request Signer
   * @param responseHandler
   *   the response decoder
   * @tparam DE
   *   the Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, PaginatedReleases]]`
   */
  def getArtistReleases[DE](artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])(signer: Signer)(
      implicit responseHandler: ResponseHandler[DE, PaginatedReleases]
  ): F[SttpResponse[DE, PaginatedReleases]] = {

    val uri: Uri =
      (artistsPath / artistId.toString / "releases")
        .withOptionQueryParam("sort", sortBy.map(_.entryName))
        .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))

    baseRequest(userAgent)
      .get(uri)
      .sign(signer)
      .response(responseHandler)
      .send(backend)
  }
}

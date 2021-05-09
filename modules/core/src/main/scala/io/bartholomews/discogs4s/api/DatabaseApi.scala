package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.DiscogsEndpoint
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.Signer
import sttp.client3.SttpBackend
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

  def getRelease[DE](releaseId: Long, marketplaceCurrency: Option[MarketplaceCurrency] = None)(signer: Signer)(implicit
      responseHandler: ResponseHandler[DE, Release]
  ): F[SttpResponse[DE, Release]] = {
    val uri: Uri =
      (releasesPath / releaseId.toString)
        .withOptionQueryParam("curr_abbr", marketplaceCurrency.map(_.entryName))

    baseRequest(userAgent)
      .get(uri)
      .sign(signer)
      .response(responseHandler)
      .send(backend)
  }

  /**
   * https://www.discogs.com/developers/#page:database,header:database-artist-releases
   *
   * Get an artist’s releases
   *
   * @param artistId
   *   The Artist ID
   *
   * @param sortBy
   *   Sort items by this field: year (i.e. year of the release) title (i.e. title of the release) format
   *
   * @param sortOrder
   *   Sort items in a particular order (one of asc, desc)
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

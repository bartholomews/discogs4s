package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.DiscogsEndpoint
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.Signer
import sttp.client3.SttpBackend
import sttp.model.Uri

class DatabaseApi[F[_], S <: Signer](userAgent: UserAgent, backend: SttpBackend[F, Any]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  final val artistsPath = DiscogsEndpoint.apiUri / "artists"
  def artistIdPath(artistId: Int): Uri = artistsPath / artistId.toString

  /**
   * https://www.discogs.com/developers/#page:database,header:database-artist-releases
   *
   * Get an artist’s releases
   *
   * @param artistId The Artist ID
   *
   * @param sortBy Sort items by this field:
   *               year (i.e. year of the release)
   *               title (i.e. title of the release)
   *               format
   *
   * @param sortOrder Sort items in a particular order (one of asc, desc)
   * @return
   */
  def getArtistsReleases[DE](artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])(signer: S)(
    implicit responseHandler: ResponseHandler[DE, PaginatedReleases]
  ): F[SttpResponse[DE, PaginatedReleases]] = {

    val uri: Uri =
      (artistIdPath(artistId) / "releases")
        .withOptionQueryParam("sort", sortBy.map(_.entryName))
        .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))

    baseRequest(userAgent)
      .get(uri)
      .sign(signer)
      .response(responseHandler)
      .send(backend)
  }
}

package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.DiscogsEndpoint
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.Signer
import io.bartholomews.fsclient.core.{FsApiClient, FsClient}
import sttp.client3.circe.asJson
import sttp.model.Uri

class ArtistsApi[F[_], S <: Signer](client: FsClient[F, S]) extends FsApiClient(client) {

  final val basePath = DiscogsEndpoint.apiUri / "artists"
  def artistsPath(artistId: Int): Uri = basePath / artistId.toString

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
  def getArtistsReleases[E](artistId: Int,
                            sortBy: Option[SortBy],
                            sortOrder: Option[SortOrder]): F[SttpResponse[io.circe.Error, PaginatedReleases]] = {

    val uri: Uri =
      (artistsPath(artistId) / "releases")
        .withOptionQueryParam("sort", sortBy.map(_.entryName))
        .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))

    backend.send(
      baseRequest(client)
        .get(uri)
        .sign(client)
        .response(asJson[PaginatedReleases])
    )
  }
}

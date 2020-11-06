package io.bartholomews.discogs4s.api

import cats.effect.ConcurrentEffect
import io.bartholomews.discogs4s.endpoints.ArtistsReleases
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}
import io.bartholomews.fsclient.client.FsClientV1
import io.bartholomews.fsclient.entities.oauth.SignerV1
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse

class ArtistsApi[F[_]: ConcurrentEffect](client: FsClientV1[F, SignerV1]) {

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
  def getArtistsReleases(artistId: Int,
                         sortBy: Option[SortBy],
                         sortOrder: Option[SortOrder]): F[HttpResponse[PaginatedReleases]] =
    ArtistsReleases(artistId, sortBy, sortOrder).runWith(client)
}

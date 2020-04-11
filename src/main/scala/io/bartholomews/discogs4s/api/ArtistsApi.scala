package io.bartholomews.discogs4s.api

import cats.effect.Effect
import fsclient.client.effect.HttpEffectClient
import fsclient.entities.OAuthInfo.OAuthV1
import fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.discogs4s.endpoints.ArtistsReleases
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}

class ArtistsApi[F[_]: Effect](client: HttpEffectClient[F, OAuthV1]) {

  import fsclient.implicits.{emptyEntityEncoder, rawJsonPipe}

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

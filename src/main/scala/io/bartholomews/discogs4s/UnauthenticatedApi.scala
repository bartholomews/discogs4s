package io.bartholomews.discogs4s

import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.entities.PaginatedReleases

trait UnauthenticatedApi {
  /**
    * https://www.discogs.com/developers/#page:database,header:database-artist-releases
    *
    * Get an artist’s releases
    *
    * @param artistId The Artist ID
    *                 TODO Sort items by this field: `year`, `title`, `format`
    * @param page
    * @param perPage
    * @return
    */
  def getArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): IOResponse[PaginatedReleases]
}

package io.bartholomews.discogs4s.endpoints

import io.bartholomews.fsclient.requests.JsonRequest
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}
import org.http4s.Uri

sealed trait ArtistsEndpoint extends DiscogsEndpoint {
  private[endpoints] val artistId: Int
  private[endpoints] val basePath: Uri = apiUri / "artists" / artistId.toString
}

case class ArtistsReleases(artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])
    extends ArtistsEndpoint
    with JsonRequest.Get[PaginatedReleases] {
  override val uri: Uri =
    (basePath / "releases")
      .withOptionQueryParam("sort", sortBy.map(_.entryName))
      .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))
}

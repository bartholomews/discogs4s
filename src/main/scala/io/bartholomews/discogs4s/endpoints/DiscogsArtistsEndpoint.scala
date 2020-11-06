package io.bartholomews.discogs4s.endpoints

import io.bartholomews.discogs4s.endpoints.DiscogsArtistsEndpoint._
import io.bartholomews.discogs4s.entities.{PaginatedReleases, SortBy, SortOrder}
import io.bartholomews.fsclient.requests.FsSimpleJson
import org.http4s.Uri

sealed trait DiscogsArtistsEndpoint {
  def artistId: Int
  final val artistsUri: Uri = basePath / artistId.toString
}

object DiscogsArtistsEndpoint {
  final val basePath = DiscogsEndpoint.apiUri / "artists"
}

case class ArtistsReleases(artistId: Int, sortBy: Option[SortBy], sortOrder: Option[SortOrder])
    extends FsSimpleJson.Get[PaginatedReleases]
    with DiscogsArtistsEndpoint {
  override val uri: Uri =
    (artistsUri / "releases")
      .withOptionQueryParam("sort", sortBy.map(_.entryName))
      .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))
}

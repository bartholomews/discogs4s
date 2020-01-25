package io.bartholomews.discogs4s.api

import fsclient.requests.JsonRequest
import io.bartholomews.discogs4s.entities.PaginatedReleases
import org.http4s.Uri

sealed trait ArtistsApi extends DiscogsEndpoint {
  private[api] val artistId: Int
  private[api] val basePath: Uri = apiUri / "artists" / artistId.toString
}

case class ArtistsReleases(artistId: Int, page: Int, perPage: Int)
  extends ArtistsApi with JsonRequest.Get[PaginatedReleases] {
  override val uri: Uri =
    (basePath / "new-releases")
      .withQueryParam("page", page)
      .withQueryParam("per_page", perPage)
}

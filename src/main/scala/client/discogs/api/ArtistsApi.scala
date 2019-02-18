package client.discogs.api

import client.discogs.entities.{DiscogsEntity, PaginatedReleases}
import org.http4s.Uri

sealed trait ArtistsApi[T <: DiscogsEntity] extends DiscogsEndpoint[T] {
  private[api] val artistId: Int
  private[api] val basePath: Uri = apiUri / "artists" / artistId.toString
}

case class ArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2)
  extends ArtistsApi[PaginatedReleases] with HttpMethod.GET {
  override val uri: Uri =
    (basePath / "releases")
      .withQueryParam("page", page)
      .withQueryParam("per_page", perPage)
}

package client.discogs.api

import client.discogs.entities.{DiscogsEntity, PaginatedReleases}
import io.circe.Decoder
import org.http4s.Uri

sealed trait ArtistsApi[T <: DiscogsEntity] extends DiscogsApi[T] {
  private[api] val artistId: Int
  private[api] val basePath: Uri = apiUri / "artists" / artistId.toString
}

case class ArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2)
                          (implicit decoder: Decoder[PaginatedReleases]) extends ArtistsApi[PaginatedReleases] {
  override val uri: Uri =
    (basePath / "releases")
      .withQueryParam("page", page)
      .withQueryParam("per_page", perPage)
}

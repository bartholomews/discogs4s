package client

import entities.{DiscogsEntity, PaginatedReleases}
import io.circe.Decoder
import org.http4s.Uri
import utils.Config

sealed trait Api[T <: DiscogsEntity] {
  val uri: Uri
  private[client] val baseUrl =
    Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}")
}

sealed trait ArtistsApi[T <: DiscogsEntity] extends Api[T] {
  private[client] val artistId: Int
  private[client] val basePath: Uri = baseUrl / "artists" / artistId.toString
}

case class ArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2)
                          (implicit decoder: Decoder[PaginatedReleases]) extends ArtistsApi[PaginatedReleases] {

  override val uri: Uri =
    (basePath / "releases")
      .withQueryParam("page", page)
      .withQueryParam("per_page", perPage)
}

package io.bartholomews.discogs4s.data

import io.bartholomews.discogs4s.entities.ArtistRelease
import sttp.client3.UriContext

object ArtistReleases {
  def make(
      id: Long,
      name: String,
      role: String,
      anv: String = ""
  ): ArtistRelease =
    ArtistRelease(
      name,
      anv = anv,
      join = "",
      role,
      tracks = "",
      id,
      resourceUrl = uri"https://api.discogs.com/artists/$id"
    )
}

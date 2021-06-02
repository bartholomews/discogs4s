package io.bartholomews.discogs4s.data

import io.bartholomews.discogs4s.entities.DiscogsImage
import sttp.client3.UriContext

object ReleaseImages {
  def make(`type`: String, width: Int, height: Int): DiscogsImage = DiscogsImage(
    `type`,
    resourceUrl = uri"",
    uri = uri"",
    uri150 = uri"",
    width = width,
    height = height
  )
}

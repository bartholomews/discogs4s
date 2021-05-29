package io.bartholomews.discogs4s.data

import io.bartholomews.discogs4s.entities.ReleaseImage
import sttp.client3.UriContext

object ReleaseImages {
  def make(`type`: String, width: Int, height: Int): ReleaseImage = ReleaseImage(
    `type`,
    resourceUrl = uri"",
    uri = uri"",
    uri150 = uri"",
    width = width,
    height = height
  )
}

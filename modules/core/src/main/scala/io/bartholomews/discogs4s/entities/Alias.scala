package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class Alias(id: Long, name: String, resourceUrl: Uri, thumbnailUrl: Option[Uri])

package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class Group(id: Long, name: String, resourceUrl: Uri, active: Boolean, thumbnailUrl: Option[Uri])

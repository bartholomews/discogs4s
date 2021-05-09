package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class ReleaseVideo(uri: Uri, title: String, description: String, duration: Double, embed: Boolean)

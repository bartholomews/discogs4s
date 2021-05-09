package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class ArtistRelease(
    name: String,
    anv: String,
    join: String,
    role: String,
    tracks: String,
    id: Long,
    resourceUrl: Uri
)

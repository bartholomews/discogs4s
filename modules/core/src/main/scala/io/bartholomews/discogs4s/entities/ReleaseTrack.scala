package io.bartholomews.discogs4s.entities

final case class ReleaseTrack(
    position: String,
    `type_`: String,
    title: String,
    duration: String,
    extraartists: List[ArtistRelease] // TODO: empty if not found
)

package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class Artist(
    name: String,
    id: ArtistId,
    resourceUrl: Uri,
    uri: Uri,
    releasesUrl: Uri,
    images: List[DiscogsImage],
    realname: String,
    profile: String,
    urls: List[Uri],
    namevariations: List[String],
    aliases: List[Alias],
    groups: List[Group],
    dataQuality: String
)

final case class ArtistId(value: Long) extends AnyVal

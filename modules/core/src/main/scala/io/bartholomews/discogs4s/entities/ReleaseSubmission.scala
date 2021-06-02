package io.bartholomews.discogs4s.entities

import java.time.LocalDateTime

import sttp.model.Uri

final case class ReleaseSubmission(
    artists: List[ArtistRelease],
    community: CommunityReleaseSubmission,
    country: Option[String],
    dataQuality: String,
    dateAdded: LocalDateTime,
    dateChanged: LocalDateTime,
    estimatedWeight: Option[Int],
    formatQuantity: Int,
    formats: List[ReleaseFormat],
    genres: List[Genre],
    id: Long,
    images: List[DiscogsImage],
    labels: List[EntityResource],
    masterId: Option[Long],
    masterUrl: Option[String],
    notes: Option[String],
    released: Option[String],
    releasedFormatted: Option[String],
    resourceUrl: Uri,
    status: ReleaseStatus,
    styles: List[Style],
    thumb: Uri,
    title: String,
    uri: Uri,
    videos: List[ReleaseVideo],
    year: Int
)

final case class Style(value: String) extends AnyVal
final case class Genre(value: String) extends AnyVal

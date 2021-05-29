package io.bartholomews.discogs4s.entities

final case class ArtistReleaseSubmission(
    status: Option[ReleaseStatus],
    mainRelease: Option[Int],
    thumb: String,
    title: String,
    format: Option[String],
    label: Option[String],
    role: String,
    year: Option[Int],
    resourceUrl: String,
    artist: String,
    `type`: String,
    id: Long
)

final case class PaginatedReleases(pagination: Pagination, releases: List[ArtistReleaseSubmission])
    extends DiscogsEntity

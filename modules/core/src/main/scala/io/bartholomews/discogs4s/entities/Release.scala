package io.bartholomews.discogs4s.entities

case class Release(status: Option[String],
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
                   id: Long)

case class PaginatedReleases(pagination: Pagination, releases: Seq[Release]) extends DiscogsEntity

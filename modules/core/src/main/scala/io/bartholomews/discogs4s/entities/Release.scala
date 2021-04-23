package io.bartholomews.discogs4s.entities

case class Release(status: Option[String],
                   main_release: Option[Int],
                   thumb: String,
                   title: String,
                   format: Option[String],
                   label: Option[String],
                   role: String,
                   year: Option[Int],
                   resource_url: String,
                   artist: String,
                   `type`: String,
                   id: Long)

case class PaginatedReleases(pagination: Pagination, releases: Seq[Release]) extends DiscogsEntity

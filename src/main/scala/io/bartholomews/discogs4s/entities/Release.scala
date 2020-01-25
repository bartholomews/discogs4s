package io.bartholomews.discogs4s.entities

import fsclient.codecs.FsJsonResponsePipe
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class Release(status: Option[String],
                   main_release: Option[Int],
                   thumb: String,
                   title: String,
                   format: Option[String],
                   label: Option[String],
                   role: String,
                   year: Int,
                   resource_url: String,
                   artist: String,
                   `type`: String,
                   id: Long)

object Release extends FsJsonResponsePipe[Release] {
  implicit val decoder: Decoder[Release] = deriveDecoder[Release]
}

case class PaginatedReleases(pagination: Pagination, releases: Seq[Release]) extends DiscogsEntity

object PaginatedReleases extends FsJsonResponsePipe[PaginatedReleases] {
  implicit val decoder: Decoder[PaginatedReleases] = deriveDecoder[PaginatedReleases]
}
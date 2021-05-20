package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.DatabaseApiSpec
import io.bartholomews.discogs4s.entities.{PaginatedReleases, Release, ReleaseRating}
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class DatabaseApiPlaySpec
    extends DatabaseApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs {
  implicit override val paginatedReleasesDecoder: Reads[PaginatedReleases] = paginatedReleasesReads
  implicit override val releaseDecoder: Reads[Release]                     = releaseCodec
  implicit override val releaseRatingDecoder: Reads[ReleaseRating]         = releaseRatingCodec
}
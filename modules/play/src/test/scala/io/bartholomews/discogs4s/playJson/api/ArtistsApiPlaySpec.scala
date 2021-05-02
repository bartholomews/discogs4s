package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.ArtistsApiSpec
import io.bartholomews.discogs4s.entities.PaginatedReleases
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class ArtistsApiPlaySpec
    extends ArtistsApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs {
  implicit override def paginatedReleasesDecoder: Reads[PaginatedReleases] = paginatedReleasesReads
}

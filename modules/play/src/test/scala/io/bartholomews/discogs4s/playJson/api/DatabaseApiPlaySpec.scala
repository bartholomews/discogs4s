package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.DatabaseApiSpec
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class DatabaseApiPlaySpec
    extends DatabaseApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs {
  implicit override val paginatedReleasesDecoder: Reads[PaginatedReleases]         = paginatedReleasesReads
  implicit override val releaseDecoder: Reads[Release]                             = releaseCodec
  implicit override val releaseRatingDecoder: Reads[ReleaseRating]                 = releaseRatingCodec
  implicit override val communityReleaseDecoder: Reads[CommunityRelease]           = communityReleaseCodec
  implicit override val communityReleaseStatsDecoder: Reads[CommunityReleaseStats] = communityReleaseStatsCodec
  implicit override val masterReleaseDecoder: Reads[MasterRelease]                 = masterReleaseCodec
  implicit override val masterReleaseVersionsDecoder: Reads[MasterReleaseVersions] = masterReleaseVersionsCodec
  implicit override val artistDecoder: Reads[Artist]                               = artistCodec
}

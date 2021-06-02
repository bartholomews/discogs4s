package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.circe.{CirceEntityCodecs, CirceServerBehaviours}
import io.bartholomews.discogs4s.entities._
import io.circe
import io.circe.Decoder

class DatabaseApiCirceSpec
    extends DatabaseApiSpec[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with CirceServerBehaviours
    with CirceEntityCodecs {
  implicit override val releaseDecoder: Decoder[Release]                             = releaseCodec
  implicit override val releaseRatingDecoder: Decoder[ReleaseRating]                 = releaseRatingCodec
  implicit override val communityReleaseDecoder: Decoder[CommunityRelease]           = communityReleaseCodec
  implicit override val communityReleaseStatsDecoder: Decoder[CommunityReleaseStats] = communityReleaseStatsCodec
  implicit override val masterReleaseDecoder: Decoder[MasterRelease]                 = masterReleaseCodec
  implicit override val masterReleaseVersionsDecoder: Decoder[MasterReleaseVersions] = masterReleaseVersionsCodec
  implicit override val artistDecoder: Decoder[Artist]                               = artistCodec
}

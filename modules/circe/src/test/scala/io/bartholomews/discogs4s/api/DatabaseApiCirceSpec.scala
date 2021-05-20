package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.circe.{CirceEntityCodecs, CirceServerBehaviours}
import io.bartholomews.discogs4s.entities.{Release, ReleaseRating}
import io.circe
import io.circe.Decoder

class DatabaseApiCirceSpec
    extends DatabaseApiSpec[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with CirceServerBehaviours
    with CirceEntityCodecs {
  implicit override val releaseDecoder: Decoder[Release]             = releaseCodec
  implicit override val releaseRatingDecoder: Decoder[ReleaseRating] = releaseRatingCodec
}

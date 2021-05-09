package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.circe.{CirceEntityCodecs, CirceServerBehaviours}
import io.bartholomews.discogs4s.entities.{UserContributions, UserSubmissionResponse}
import io.circe
import io.circe.Decoder

class UsersApiCirceSpec
    extends UsersApiSpec[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with CirceServerBehaviours
    with CirceEntityCodecs {
  implicit override val userSubmissionResponseDecoder: Decoder[UserSubmissionResponse] = userSubmissionResponseCodec
  implicit override val userContributionsDecoder: Decoder[UserContributions]           = userContributionsCodec
}

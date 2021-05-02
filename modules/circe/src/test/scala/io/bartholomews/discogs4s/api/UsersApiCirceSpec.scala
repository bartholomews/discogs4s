package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.circe.{CirceEntityCodecs, CirceServerBehaviours}
import io.circe

class UsersApiCirceSpec
    extends UsersApiSpec[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with CirceServerBehaviours
    with CirceEntityCodecs

package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.circe.{CirceEntityCodecs, CirceServerBehaviours}
import io.circe

class ArtistsApiCirceSpec
    extends ArtistsApiSpec[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with CirceServerBehaviours
    with CirceEntityCodecs

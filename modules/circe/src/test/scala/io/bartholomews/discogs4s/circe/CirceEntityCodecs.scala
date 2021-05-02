package io.bartholomews.discogs4s.circe

import io.bartholomews.discogs4s.client.DiscogsClientData
import io.bartholomews.scalatestudo.entities.JsonCodecs
import io.circe.generic.semiauto

trait CirceEntityCodecs {
  import io.circe._
  import io.circe.syntax.EncoderOps

  implicit def discogsErrorEncoder: Encoder[DiscogsClientData.DiscogsError] =
    semiauto.deriveEncoder[DiscogsClientData.DiscogsError]

  implicit def discogsErrorDecoder: Decoder[DiscogsClientData.DiscogsError] =
    semiauto.deriveDecoder[DiscogsClientData.DiscogsError]

  implicit def entityCodecs[Entity](
    implicit encoder: Encoder[Entity],
    decoder: Decoder[Entity]
  ): JsonCodecs[Entity, Encoder, Decoder, Json] =
    new JsonCodecs[Entity, Encoder, Decoder, Json] {
      implicit override def entityEncoder: Encoder[Entity] = encoder
      implicit override def entityDecoder: Decoder[Entity] = decoder
      override def encode(entity: Entity): Json = entity.asJson(encoder)
      override def decode(json: Json): Either[String, Entity] = json.as[Entity](decoder).left.map(_.message)
      override def parse(rawJson: String): Either[String, Json] =
        io.circe.parser.parse(rawJson).left.map(_.message)
    }
}

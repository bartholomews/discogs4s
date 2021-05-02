package io.bartholomews.discogs4s.circe

import io.bartholomews.scalatestudo.{ServerBehaviours, WireWordSpec}
import io.circe
import io.circe.{Decoder, Encoder}
import sttp.client3.BodySerializer

trait CirceServerBehaviours
    extends ServerBehaviours[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with DiscogsCirceApi {

  self: WireWordSpec =>

  implicit def bodySerializer[T](implicit encoder: Encoder[T]): BodySerializer[T] =
    circeBodySerializer

  final def circeParser[B](input: String)(implicit decoder: Decoder[B]): Either[circe.Error, B] =
    io.circe.parser.parse(input).flatMap(_.as[B](decoder))
}

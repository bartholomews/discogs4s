package io.bartholomews.discogs4s.circe

import io.bartholomews.discogs4s.DiscogsServerBehaviours
import io.bartholomews.scalatestudo.WireWordSpec
import io.circe
import io.circe.{Decoder, Encoder}
import sttp.client3.BodySerializer

import scala.reflect.ClassTag

trait CirceServerBehaviours
    extends DiscogsServerBehaviours[circe.Encoder, circe.Decoder, circe.Error, circe.Json]
    with DiscogsCirceApi {

  self: WireWordSpec =>

  implicit override val ct: ClassTag[circe.Error] = ClassTag[circe.Error](circe.Error.getClass)

  implicit def bodySerializer[T](implicit encoder: Encoder[T]): BodySerializer[T] =
    circeBodySerializer

  final def circeParser[B](input: String)(implicit decoder: Decoder[B]): Either[circe.Error, B] =
    io.circe.parser.parse(input).flatMap(_.as[B](decoder))
}

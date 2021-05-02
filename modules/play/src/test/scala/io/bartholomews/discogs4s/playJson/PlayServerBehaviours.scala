package io.bartholomews.discogs4s.playJson

import io.bartholomews.scalatestudo.{ServerBehaviours, WireWordSpec}
import play.api.libs.json._
import sttp.client3.BodySerializer

trait PlayServerBehaviours extends ServerBehaviours[Writes, Reads, JsError, JsValue] with DiscogsPlayJsonApi {
  self: WireWordSpec =>

  implicit def bodySerializer[T](implicit encoder: Writes[T]): BodySerializer[T] =
    sttp.client3.playJson.playJsonBodySerializer

  final def playParser[B](input: String)(implicit rds: Reads[B]): Either[JsError, B] =
    Json.parse(input).validate[B] match {
      case JsSuccess(value, _) => Right(value)
      case error: JsError      => Left(error)
    }
}

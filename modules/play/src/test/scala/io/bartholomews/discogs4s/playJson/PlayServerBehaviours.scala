package io.bartholomews.discogs4s.playJson

import io.bartholomews.discogs4s.DiscogsServerBehaviours
import io.bartholomews.scalatestudo.WireWordSpec
import play.api.libs.json._
import sttp.client3.BodySerializer

import scala.reflect.ClassTag

trait PlayServerBehaviours extends DiscogsServerBehaviours[Writes, Reads, JsError, JsValue] with DiscogsPlayJsonApi {
  self: WireWordSpec =>

  implicit override val ct: ClassTag[JsError] = ClassTag[JsError](JsError.getClass)

  implicit def bodySerializer[T](implicit encoder: Writes[T]): BodySerializer[T] =
    sttp.client3.playJson.playJsonBodySerializer

  final def playParser[B](input: String)(implicit rds: Reads[B]): Either[JsError, B] =
    Json.parse(input).validate[B] match {
      case JsSuccess(value, _) => Right(value)
      case error: JsError      => Left(error)
    }
}

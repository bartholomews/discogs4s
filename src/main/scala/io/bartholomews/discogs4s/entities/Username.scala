package io.bartholomews.discogs4s.entities

import io.circe.Decoder
import io.circe.generic.extras._

case class Username(value: String) extends AnyVal
object Username { implicit val decoder: Decoder[Username] = semiauto.deriveUnwrappedDecoder }

case class UserEmail(value: String) extends AnyVal
object UserEmail { implicit val decoder: Decoder[UserEmail] = semiauto.deriveUnwrappedDecoder }

case class UserRealName(value: String) extends AnyVal
object UserRealName { implicit val decoder: Decoder[UserRealName] = semiauto.deriveUnwrappedDecoder }

case class UserWebsite(value: String) extends AnyVal
object UserWebsite { implicit val decoder: Decoder[UserWebsite] = semiauto.deriveUnwrappedDecoder }

case class UserLocation(value: String) extends AnyVal
object UserLocation { implicit val decoder: Decoder[UserLocation] = semiauto.deriveUnwrappedDecoder }

case class UserProfileInfo(value: String) extends AnyVal
object UserProfileInfo { implicit val decoder: Decoder[UserProfileInfo] = semiauto.deriveUnwrappedDecoder }

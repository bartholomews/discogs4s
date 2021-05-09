package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class Username(value: String) extends AnyVal
final case class UserEmail(value: String) extends AnyVal
final case class UserRealName(value: String) extends AnyVal
final case class UserWebsite(value: String) extends AnyVal
final case class UserLocation(value: String) extends AnyVal
final case class UserProfileInfo(value: String) extends AnyVal

final case class UserResource(username: Username, resourceUrl: Uri)

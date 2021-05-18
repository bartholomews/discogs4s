package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class DiscogsUserId(value: Long)            extends AnyVal
final case class DiscogsUsername(value: String)        extends AnyVal
final case class DiscogsUserEmail(value: String)       extends AnyVal
final case class DiscogsUserRealName(value: String)    extends AnyVal
final case class DiscogsUserWebsite(value: String)     extends AnyVal
final case class DiscogsUserLocation(value: String)    extends AnyVal
final case class DiscogsUserProfileInfo(value: String) extends AnyVal

final case class DiscogsUserResource(username: DiscogsUsername, resourceUrl: Uri)

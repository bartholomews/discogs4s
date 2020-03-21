package io.bartholomews.discogs4s.api

import fsclient.requests.{FsAuthRequest, JsonRequest}
import io.bartholomews.discogs4s.entities.{AuthenticatedUser, SimpleUser}
import io.circe.Json
import org.http4s.Uri

sealed trait UserApi extends DiscogsEndpoint {
  private[api] val username: String
  private[api] val basePath: Uri = apiUri / "users"
}

case class GetSimpleUserProfile(username: String) extends UserApi with JsonRequest.Get[SimpleUser] {
  override val uri: Uri = basePath / username
}

case class GetAuthenticatedUserProfile(username: String) extends UserApi with JsonRequest.Get[AuthenticatedUser] {
  override val uri: Uri = basePath / username
}

// TODO
case class UpdateUserProfile(
  username: String,
  name: Option[String],
  homePage: Option[String],
  location: Option[String],
  profile: Option[String],
  currAbbr: Option[String]
) extends UserApi
    with FsAuthRequest.Post[Nothing, Json, AuthenticatedUser] {
  final override val body: Option[Nothing] = None
  final override val uri: Uri = {
    (basePath / username)
      .withOptionQueryParam("location", location)
  }
}

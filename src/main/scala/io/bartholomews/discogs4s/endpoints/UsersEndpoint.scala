package io.bartholomews.discogs4s.endpoints

import fsclient.requests.{AuthJsonRequest, FsAuthRequest, JsonRequest}
import io.bartholomews.discogs4s.entities.{
  AuthenticatedUser,
  MarketplaceCurrency,
  SimpleUser,
  UserLocation,
  UserProfileInfo,
  UserRealName,
  UserWebsite,
  Username
}
import io.circe.Json
import org.http4s.Uri

sealed trait UsersEndpoint extends DiscogsEndpoint {
  private[endpoints] def username: Username
  private[endpoints] def basePath: Uri = apiUri / "users"
}

case class GetSimpleUserProfile(username: Username) extends UsersEndpoint with JsonRequest.Get[SimpleUser] {
  override val uri: Uri = basePath / username.value
}

case class GetAuthenticatedUserProfile(username: Username)
    extends UsersEndpoint
    with AuthJsonRequest.Get[AuthenticatedUser] {
  override val uri: Uri = basePath / username.value
}

case class UpdateUserProfile(
  username: Username,
  name: Option[UserRealName],
  homePage: Option[UserWebsite],
  location: Option[UserLocation],
  profile: Option[UserProfileInfo],
  currAbbr: Option[MarketplaceCurrency]
) extends UsersEndpoint
    with FsAuthRequest.Post[Nothing, Json, AuthenticatedUser] {

  final override val body: Option[Nothing] = None
  final override val uri: Uri = {
    (basePath / username.value)
      .withOptionQueryParam("name", name.map(_.value))
      .withOptionQueryParam("home_page", homePage.map(_.value))
      .withOptionQueryParam("location", location.map(_.value))
      .withOptionQueryParam("profile", profile.map(_.value))
      .withOptionQueryParam("curr_abbr", currAbbr.map(_.entryName))
  }
}

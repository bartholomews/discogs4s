package io.bartholomews.discogs4s.endpoints

import io.bartholomews.discogs4s.endpoints.DiscogsUsersEndpoint._
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
import io.bartholomews.fsclient.requests.{FsAuthJson, FsSimpleJson}
import org.http4s.Uri

sealed trait DiscogsUsersEndpoint
object DiscogsUsersEndpoint {
  final val basePath: Uri = DiscogsEndpoint.apiUri / "users"
}

case class GetSimpleUserProfile(username: Username) extends FsSimpleJson.Get[SimpleUser] with DiscogsUsersEndpoint {
  override val uri: Uri = basePath / username.value
}

case class GetAuthenticatedUserProfile(username: Username)
    extends FsAuthJson.Get[AuthenticatedUser]
    with DiscogsUsersEndpoint {
  override val uri: Uri = basePath / username.value
}

case class UpdateUserProfile(
  username: Username,
  name: Option[UserRealName],
  homePage: Option[UserWebsite],
  location: Option[UserLocation],
  profile: Option[UserProfileInfo],
  currAbbr: Option[MarketplaceCurrency]
) extends FsAuthJson.PostEmpty[AuthenticatedUser]
    with DiscogsUsersEndpoint {
  final override val uri: Uri = {
    (basePath / username.value)
      .withOptionQueryParam("name", name.map(_.value))
      .withOptionQueryParam("home_page", homePage.map(_.value))
      .withOptionQueryParam("location", location.map(_.value))
      .withOptionQueryParam("profile", profile.map(_.value))
      .withOptionQueryParam("curr_abbr", currAbbr.map(_.entryName))
  }
}

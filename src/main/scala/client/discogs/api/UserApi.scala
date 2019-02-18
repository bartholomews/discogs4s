package client.discogs.api

import client.discogs.entities.{AuthenticatedUser, DiscogsEntity, SimpleUser}
import org.http4s.Uri

sealed trait UserApi[T <: DiscogsEntity] extends DiscogsEndpoint[T] {
  private[api] val username: String
  private[api] val basePath: Uri = apiUri / "users"
}

case class GetSimpleUserProfile(username: String) extends UserApi[SimpleUser] with HttpMethod.GET {
  override val uri: Uri = basePath / username
}

case class GetAuthenticatedUserProfile(username: String) extends UserApi[AuthenticatedUser] with HttpMethod.GET {
  override val uri: Uri = basePath / username
}

// TODO
case class UpdateUserProfile(username: String, location: String) extends UserApi[AuthenticatedUser] with HttpMethod.POST {
  override val uri: Uri = {
    (basePath / username)
      .withQueryParam("location", location)
  }
}
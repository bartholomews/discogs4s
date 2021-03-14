package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.{DiscogsAuthEndpoint, DiscogsEndpoint}
import io.bartholomews.discogs4s.entities.{
  AuthenticatedUser,
  MarketplaceCurrency,
  SimpleUser,
  UserIdentity,
  UserLocation,
  UserProfileInfo,
  UserRealName,
  UserWebsite,
  Username
}
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.{OAuthSigner, Signer, SignerV1}
import sttp.client3.circe.asJson
import sttp.model.Uri

class UsersApi[F[_], S <: Signer](client: FsClient[F, S]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  type DE = io.circe.Error

  private val basePath: Uri = DiscogsEndpoint.apiUri / "users"
  private def userPath(username: Username): Uri = basePath / username.value

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
   *
   * Retrieve a user by username.
   *
   * If authenticated as the requested user or the user’s collection/wantlist is public,
   * the num_collection / num_wantlist keys will be visible.
   *
   * @param username The username of whose profile you are requesting.
   * @return `SimpleUser`
   */
  def getSimpleUserProfile(username: Username): F[SttpResponse[DE, SimpleUser]] =
    baseRequest(client.userAgent)
      .get(userPath(username))
      .sign(client)
      .response(asJson[SimpleUser])
      .send(client.backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
   *
   * Retrieve a user by username.
   * If authenticated as the requested user, the email key will be visible,
   * and the num_list count will include the user’s private lists.
   * Otherwise the call would probably fail since the entity expects the extra fields in the response.
   * For unauthenticated calls `getSimpleUserProfile` should be used instead, returning a `SimpleUser` entity.
   *
   * If authenticated as the requested user or the user’s collection/wantlist is public,
   * the num_collection / num_wantlist keys will be visible.
   *
   * @param username The username of whose profile you are requesting.
   * @return `AuthenticatedUser`
   */
  def getAuthenticateUserProfile(
    username: Username
  )(implicit signer: SignerV1): F[SttpResponse[DE, AuthenticatedUser]] =
    baseRequest(client.userAgent)
      .get(userPath(username))
      .sign
      .response(asJson[AuthenticatedUser])
      .send(client.backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post
   *
   * Edit a user’s profile data.
   *
   * @param username The username of the user.
   * @param name The real name of the user.
   * @param homePage The user’s website.
   * @param location The geographical location of the user.
   * @param profile Biographical information about the user.
   * @param currAbbr Currency for marketplace data. Must be one of the following:
   *                USD GBP EUR CAD AUD JPY CHF MXN BRL NZD SEK ZAR
   *
   * @return `AuthenticatedUser`
   */
  def updateUserProfile(
    username: Username,
    name: Option[UserRealName],
    homePage: Option[UserWebsite],
    location: Option[UserLocation],
    profile: Option[UserProfileInfo],
    currAbbr: Option[MarketplaceCurrency]
  )(implicit signer: SignerV1): F[SttpResponse[DE, AuthenticatedUser]] =
    baseRequest(client.userAgent)
      .post(
        userPath(username)
          .withOptionQueryParam("name", name.map(_.value))
          .withOptionQueryParam("home_page", homePage.map(_.value))
          .withOptionQueryParam("location", location.map(_.value))
          .withOptionQueryParam("profile", profile.map(_.value))
          .withOptionQueryParam("curr_abbr", currAbbr.map(_.entryName))
      )
      .sign
      .response(asJson[AuthenticatedUser])
      .send(client.backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-identity
   *
   * Retrieve basic information about the authenticated user.
   * You can use this resource to find out who you’re authenticated as,
   * and it also doubles as a good sanity check to ensure that you’re using OAuth correctly.
   * For more detailed information, make another request for the user’s Profile.
   *
   * @return `UserIdentity`
   */
  def me(implicit signer: OAuthSigner): F[SttpResponse[DE, UserIdentity]] =
    baseRequest(client.userAgent)
      .get(DiscogsEndpoint.apiUri / DiscogsAuthEndpoint.path / "identity")
      .sign
      .response(asJson[UserIdentity])
      .send(client.backend)
}

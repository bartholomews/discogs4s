package io.bartholomews.discogs4s.api

import cats.effect.ConcurrentEffect
import io.bartholomews.discogs4s.endpoints.{GetAuthenticatedUserProfile, GetSimpleUserProfile, UpdateUserProfile}
import io.bartholomews.discogs4s.entities.{AuthenticatedUser, MarketplaceCurrency, SimpleUser, UserLocation, UserProfileInfo, UserRealName, UserWebsite, Username}
import io.bartholomews.fsclient.client.FsClientV1
import io.bartholomews.fsclient.entities.oauth.SignerV1
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse

class UsersApi[F[_]: ConcurrentEffect](client: FsClientV1[F, SignerV1]) {

  import io.bartholomews.fsclient.implicits.{emptyEntityEncoder, rawJsonPipe}

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
  def getSimpleUserProfile(username: Username): F[HttpResponse[SimpleUser]] =
    GetSimpleUserProfile(username).runWith(client)

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
  )(implicit signer: SignerV1): F[HttpResponse[AuthenticatedUser]] =
    GetAuthenticatedUserProfile(username).runWith(client)

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
  )(implicit signer: SignerV1): F[HttpResponse[AuthenticatedUser]] =
    UpdateUserProfile(username, name, homePage, location, profile, currAbbr).runWith(client)
}

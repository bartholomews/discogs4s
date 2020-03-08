package io.bartholomews.discogs4s

import fsclient.entities.HttpResponse
import io.bartholomews.discogs4s.entities.{AuthenticatedUser, UserIdentity}

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
trait AuthenticatedApi[F[_]] {

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
  def me(): F[HttpResponse[UserIdentity]]

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
   *
   * Retrieve a user by username.
   * If authenticated as the requested user, the email key will be visible,
   * and the num_list count will include the user’s private lists.
   *
   * If authenticated as the requested user or the user’s collection/wantlist is public,
   * the num_collection / num_wantlist keys will be visible.
   *
   * @param username The username of whose profile you are requesting.
   * @return `AuthenticatedUser`
   */
  def getUserProfile(username: String): F[HttpResponse[AuthenticatedUser]]

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post
   *
   * Edit a user’s profile data.
   *
   * @param username The username of the user.
   * @param name The real name of the user.
   * @param location The geographical location of the user.
   * @param homePage The user’s website.
   * @param profile Biographical information about the user.
   * @param currAbbr Currency for marketplace data. Must be one of the following:
   *                USD GBP EUR CAD AUD JPY CHF MXN BRL NZD SEK ZAR
   *
   * @return `AuthenticatedUser`
   *  FIXME: Use at least value classes (or see if you can do better than that)
   *   and double check query params etc
   */
  def updateUserProfile(username: String,
                        name: Option[String],
                        homePage: Option[String],
                        location: Option[String],
                        profile: Option[String],
                        currAbbr: Option[String]): F[HttpResponse[AuthenticatedUser]]
}

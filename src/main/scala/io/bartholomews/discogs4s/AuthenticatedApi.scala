package io.bartholomews.discogs4s

import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthEnabled
import fsclient.entities.AuthVersion.V1
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.entities.{AuthenticatedUser, UserIdentity}
import org.http4s.client.oauth1.{Consumer, Token}

import scala.concurrent.ExecutionContext

trait AuthenticatedApi {

  /**
   * @return
   */
  def me(): IOResponse[UserIdentity]

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
   * @return `String`
   */
  def getUserProfile(username: String): IOResponse[AuthenticatedUser]

  /**
   * @param username
   * @param location
   * @return
   */
  def updateUserProfile(username: String, location: String): IOResponse[AuthenticatedUser]
}

object AuthenticatedApi {

  // FIXME
  def apply(userAgent: UserAgent, accessToken: V1.AccessToken)(implicit ec: ExecutionContext): AuthenticatedApi =
    new DiscogsClient(FsClientConfig(userAgent, AuthEnabled(accessToken))).authEndpoints(accessToken)

  def apply(
    userAgent: UserAgent,
    token: Token
  )(implicit consumer: Consumer, ec: ExecutionContext): AuthenticatedApi = apply(userAgent, V1.AccessToken(token))
}

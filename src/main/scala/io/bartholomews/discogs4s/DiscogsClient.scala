package io.bartholomews.discogs4s

import cats.effect.IO
import fsclient.client.io_client.IOAuthClient
import fsclient.config.FsClientConfig
import fsclient.entities.AuthEnabled
import fsclient.entities.AuthVersion.V1
import fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.api._
import io.bartholomews.discogs4s.entities.{AuthenticatedUser, PaginatedReleases, RequestTokenResponse, SimpleUser, UserIdentity}
import org.http4s.client.oauth1.Consumer

import scala.concurrent.ExecutionContext

// https://http4s.org/v0.19/streaming/
class DiscogsClient(val config: FsClientConfig[AuthEnabled])(implicit ec: ExecutionContext) {

  import DiscogsOAuthPipes._
  import fsclient.implicits._

  def this()(implicit ec: ExecutionContext) = this(FsClientConfig.v1("discogs"))

  implicit val consumer: Consumer = config.authInfo.signer.consumer

  private val client = new IOAuthClient(config.userAgent, V1.BasicSignature(config.authInfo.signer.consumer))

  // ===================================================================================================================
  // OAUTH
  // ===================================================================================================================

  def getRequestToken: IOResponse[RequestTokenResponse] = AuthorizeUrl.runWith(client)

  def getAccessToken(requestToken: V1.RequestToken): IOResponse[V1.AccessToken] =
    client.accessTokenRequest(requestToken, AccessTokenEndpoint)

  // ===================================================================================================================
  // ARTISTS API
  // ===================================================================================================================

  def getArtistsReleases(artistId: Int, page: Int, perPage: Int): IOResponse[PaginatedReleases] =
    ArtistsReleases(artistId, page, perPage).runWith(client)

  // ===================================================================================================================
  // USER API // https://www.discogs.com/developers/#page:user-identity
  // ===================================================================================================================

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
  def getUserProfile(username: String): IOResponse[SimpleUser] = GetSimpleUserProfile(username).runWith(client)

  def authEndpoints(implicit accessToken: V1.AccessToken): AuthenticatedApi[IO] = new AuthenticatedApi[IO] {

    import fsclient.implicits._

    override def me(): IOResponse[UserIdentity] = Identity.runWith(client)

    override def getUserProfile(username: String): IOResponse[AuthenticatedUser] =
      GetAuthenticatedUserProfile(username).runWith(client)

    // FIXME
    override def updateUserProfile(username: String,
                                   name: Option[String],
                                   homePage: Option[String],
                                   location: Option[String],
                                   profile: Option[String],
                                   currAbbr: Option[String]): IOResponse[AuthenticatedUser] =
      UpdateUserProfile(username, name, homePage, location, profile, currAbbr).runWith(client)
  }
}

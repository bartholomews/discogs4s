package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.endpoints.{DiscogsAuthEndpoint, DiscogsEndpoint}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.http.SttpResponses.{ResponseHandler, SttpResponse}
import io.bartholomews.fsclient.core.oauth.{OAuthSigner, Signer}
import sttp.client3.{BodySerializer, SttpBackend}
import sttp.model.Uri

/**
 * https://www.discogs.com/developers/#page:user-identity
 *
 * Retrieve basic information about the authenticated user. You can use this resource to find out who you’re
 * authenticated as, and it also doubles as a good sanity check to ensure that you’re using OAuth correctly. For more
 * detailed information, make another request for the user’s Profile.
 *
 * @param userAgent
 *   The application `User-Agent`, which will be added as header in all the requests
 * @param backend
 *   The Sttp backend for the requests
 * @tparam F
 *   The Effect type
 */
class UsersApi[F[_]](userAgent: UserAgent, backend: SttpBackend[F, Any]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  private val basePath: Uri                            = DiscogsEndpoint.apiUri / "users"
  private def userPath(username: DiscogsUsername): Uri = basePath / username.value

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-identity-get
   *
   * Retrieve basic information about the authenticated user. You can use this resource to find out who you’re
   * authenticated as, and it also doubles as a good sanity check to ensure that you’re using OAuth correctly. For more
   * detailed information, make another request for the user’s Profile.
   *
   * @param signer
   *   The request Signer
   * @param responseHandler
   *   The response decoder
   * @tparam DE
   *   The Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, UserIdentity]]`
   */
  def me[DE](signer: OAuthSigner)(implicit
      responseHandler: ResponseHandler[DE, UserIdentity]
  ): F[SttpResponse[DE, UserIdentity]] =
    baseRequest(userAgent)
      .get(DiscogsEndpoint.apiUri / DiscogsAuthEndpoint.path / "identity")
      .sign(signer)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
   *
   * Retrieve a user by username.
   *
   * If authenticated as the requested user, the email key will be visible, and the num_list count will include the
   * user’s private lists. If authenticated as the requested user or the user’s collection/wantlist is public, the
   * num_collection / num_wantlist keys will be visible.
   *
   * @param username
   *   The username of whose profile you are requesting.
   * @param signer
   *   The request Signer
   * @param responseHandler
   *   The response decoder
   * @tparam DE
   *   The Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, UserProfile]]`
   */
  def getUserProfile[DE](
      username: DiscogsUsername
  )(signer: Signer)(implicit responseHandler: ResponseHandler[DE, UserProfile]): F[SttpResponse[DE, UserProfile]] =
    baseRequest(userAgent)
      .get(userPath(username))
      .sign(signer)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post
   *
   * Edit a user’s profile data.
   *
   * @param username
   *   The username of the user.
   * @param request
   *   The real name of the user. The user’s website. The geographical location of the user. Biographical information
   *   about the user. Currency for marketplace data. Must be one of the following: USD GBP EUR CAD AUD JPY CHF MXN BRL
   *   NZD SEK ZAR
   * @param signer
   *   The request Signer
   * @param responseHandler
   *   The response decoder
   * @tparam DE
   *   The Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, AuthenticatedUser]]`
   */
  def updateUserProfile[DE](
      username: DiscogsUsername,
      request: UpdateUserRequest
  )(signer: OAuthSigner)(implicit
      bodySerializer: BodySerializer[UpdateUserRequest],
      responseHandler: ResponseHandler[DE, UserProfile]
  ): F[SttpResponse[DE, UserProfile]] =
    baseRequest(userAgent)
      .post(userPath(username))
      .body(request)
      .sign(signer)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-user-submissions-get
   *
   * Retrieve a user’s submissions by username. Accepts Pagination parameters.
   *
   * @param username
   *   The username of the submissions you are trying to fetch.
   * @param page
   *   Page to fetch (default to 1)
   * @param perPage
   *   Items per page to fetch (default to 50, max 100)
   * @param signer
   *   The request Signer
   * @param responseHandler
   *   The response decoder
   * @tparam DE
   *   The Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, UserSubmissionResponse]]`
   */
  def getUserSubmissions[DE](username: DiscogsUsername, page: Int = 1, perPage: Int = 50)(
      signer: Signer
  )(implicit
      responseHandler: ResponseHandler[DE, UserSubmissionResponse]
  ): F[SttpResponse[DE, UserSubmissionResponse]] =
    baseRequest(userAgent)
      .get(
        (userPath(username) / "submissions")
          .withQueryParam("page", page.toString)
          .withQueryParam("per_page", perPage.toString)
      )
      .sign(signer)
      .response(responseHandler)
      .send(backend)

  /**
   * https://www.discogs.com/developers/#page:user-identity,header:user-identity-user-contributions-get
   *
   * Retrieve a user’s contributions by username. Accepts Pagination parameters.
   *
   * @param username
   *   The username of the submissions you are trying to fetch.
   * @param page
   *   Page to fetch (default to 1)
   * @param perPage
   *   Items per page to fetch (default to 50, max 100)
   * @param sortBy
   *   Valid sort keys are: label artist title catno format rating year added
   * @param sortOrder
   *   Valid sort_order keys are: asc desc
   * @param signer
   *   The request Signer
   * @param responseHandler
   *   The response decoder
   * @tparam DE
   *   The Deserialization Error type
   * @return
   *   `F[SttpResponse[DE, ReleaseContributions]]`
   */
  def getUserContributions[DE](
      username: DiscogsUsername,
      page: Int = 1,
      perPage: Int = 50,
      sortBy: Option[UserContributions.SortedBy] = None,
      sortOrder: Option[SortOrder] = None
  )(
      signer: Signer
  )(implicit
      responseHandler: ResponseHandler[DE, UserContributions]
  ): F[SttpResponse[DE, UserContributions]] =
    baseRequest(userAgent)
      .get(
        (userPath(username) / "contributions")
          .withQueryParam("page", page.toString)
          .withQueryParam("per_page", perPage.toString)
          .withOptionQueryParam("sort", sortBy.map(_.entryName))
          .withOptionQueryParam("sort_order", sortOrder.map(_.entryName))
      )
      .sign(signer)
      .response(responseHandler)
      .send(backend)
}

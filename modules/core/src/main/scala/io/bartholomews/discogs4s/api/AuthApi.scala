package io.bartholomews.discogs4s.api

import cats.Applicative
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint.basePath
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.http.ResponseMapping
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth._
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.SignatureMethod
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import sttp.client3.{Response, SttpBackend}
import sttp.model.{Method, StatusCode, Uri}

/**
 * https://www.discogs.com/developers/#page:authentication For example on usages, see
 * https://github.com/bartholomews/discogs4s#full-oauth-10a-with-access-tokensecret
 * @param userAgent
 *   The application `User-Agent`, which will be added as header in all the requests
 * @param backend
 *   The Sttp backend for the requests
 * @tparam F
 *   The Effect type
 */
class AuthApi[F[_]](userAgent: UserAgent, backend: SttpBackend[F, Any]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  /**
   * Generate the request token
   * https://www.discogs.com/developers/#page:authentication,header:authentication-request-token-url
   * ----------------------------------------------------------------------------------------------
   * @param temporaryCredentialsRequest
   *   The consumer credentials and OAuth options such as redirect uri and signature method
   * @return
   *   An Sttp response with either the request token or an error
   */
  def getRequestToken(
      temporaryCredentialsRequest: TemporaryCredentialsRequest
  ): F[SttpResponse[Exception, TemporaryCredentials]] =
    temporaryCredentialsRequest.send(
      method = Method.GET,
      serverUri = basePath / "request_token",
      userAgent = userAgent,
      resourceOwnerAuthorizationUri = ResourceOwnerAuthorizationUri(DiscogsAuthEndpoint.authorizeUri)
    )(backend)

  /**
   * Generate the access token
   * https://www.discogs.com/developers/#page:authentication,header:authentication-access-token-url
   * ----------------------------------------------------------------------------------------------
   *
   * This should probably not be used directly unless you want to have a custom error (e.g. for permissions rejected or
   * some unexpected error). Otherwise you can simply use `fromUri`, which will parse the uri which the user has been
   * redirected to (after granting or rejecting permissions for the app)
   * @param signer
   *   The request token credentials
   * @return
   *   An sttp response with either the access token or an error
   */
  def getAccessToken(signer: RequestTokenCredentials): F[SttpResponse[Exception, AccessTokenCredentials]] = {

    implicit val responseMapping: ResponseMapping[String, Exception, AccessTokenCredentials] =
      AccessTokenCredentials.responseMapping(signer.consumer, signer.signatureMethod)

    baseRequest(userAgent)
      .post(basePath / "access_token")
      .sign(signer)
      .response(mapInto[String, Exception, AccessTokenCredentials])
      .send(backend)
  }

  /**
   * Generate the access token directly from the redirection uri
   * @param resourceOwnerAuthorizationUriResponse
   *   The uri where the user has been redirected to (after granting or rejecting permissions for the app)
   * @param temporaryCredentials
   *   The request token credentials obtained via `getRequestToken`
   * @param signatureMethod
   *   The OAuth signature, default to SHA1 (see https://tools.ietf.org/html/rfc5849#section-3.4)
   * @param f
   *   The effect type
   * @return
   *   An sttp response with either the access token or an error
   */
  def fromUri(
      resourceOwnerAuthorizationUriResponse: Uri,
      temporaryCredentials: TemporaryCredentials,
      signatureMethod: SignatureMethod = SignatureMethod.SHA1
  )(implicit f: Applicative[F]): F[SttpResponse[Exception, AccessTokenCredentials]] =
    RequestTokenCredentials
      .fetchRequestTokenCredentials(resourceOwnerAuthorizationUriResponse, temporaryCredentials, signatureMethod)
      .fold(
        error => f.pure(Response(code = StatusCode.Unauthorized, body = Left(error))),
        requestTokenCredentials => getAccessToken(requestTokenCredentials)
      )
}

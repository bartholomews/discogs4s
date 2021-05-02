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

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
class AuthApi[F[_]](userAgent: UserAgent, backend: SttpBackend[F, Any]) {
  import io.bartholomews.fsclient.core.http.FsClientSttpExtensions._

  def getRequestToken(
    temporaryCredentialsRequest: TemporaryCredentialsRequest
  ): F[SttpResponse[Exception, TemporaryCredentials]] =
    temporaryCredentialsRequest.send(
      method = Method.GET,
      serverUri = basePath / "request_token",
      userAgent = userAgent,
      resourceOwnerAuthorizationUri = ResourceOwnerAuthorizationUri(DiscogsAuthEndpoint.authorizeUri)
    )(backend)

  def getAccessToken(signer: RequestTokenCredentials): F[SttpResponse[Exception, AccessTokenCredentials]] = {

    implicit val responseMapping: ResponseMapping[String, Exception, AccessTokenCredentials] =
      AccessTokenCredentials.responseMapping(signer.consumer, signer.signatureMethod)

    baseRequest(userAgent)
      .post(basePath / "access_token")
      .sign(signer)
      .response(mapInto[String, Exception, AccessTokenCredentials])
      .send(backend)
  }

  def fromUri(
    resourceOwnerAuthorizationUriResponse: Uri,
    temporaryCredentials: TemporaryCredentials,
    signatureMethod: SignatureMethod = SignatureMethod.SHA1
  )(implicit f: Applicative[F]): F[SttpResponse[Exception, AccessTokenCredentials]] =
    RequestTokenCredentials
      .fetchRequestTokenCredentials(resourceOwnerAuthorizationUriResponse, temporaryCredentials, signatureMethod)
      .fold(error => f.pure(Response(code = StatusCode.Unauthorized, body = Left(error))),
            requestTokenCredentials => getAccessToken(requestTokenCredentials))
}

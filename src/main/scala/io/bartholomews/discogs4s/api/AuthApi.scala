package io.bartholomews.discogs4s.api

import cats.Applicative
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint.basePath
import io.bartholomews.fsclient.core.http.ResponseMapping
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.SignatureMethod
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.{
  AccessTokenCredentials,
  OAuthSigner,
  RequestTokenCredentials,
  ResourceOwnerAuthorizationUri,
  TemporaryCredentialsRequest
}
import io.bartholomews.fsclient.core.{FsApiClient, FsClient}
import sttp.client.{Response, ResponseError}
import sttp.model.{Method, StatusCode, Uri}

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
class AuthApi[F[_], S <: OAuthSigner](client: FsClient[F, S]) extends FsApiClient(client) {

  def getRequestToken(
    temporaryCredentialsRequest: TemporaryCredentialsRequest
  ): F[Response[Either[ResponseError[Exception], TemporaryCredentials]]] =
    temporaryCredentialsRequest.send(
      method = Method.GET,
      serverUri = basePath / "request_token",
      userAgent = client.userAgent,
      resourceOwnerAuthorizationUri = ResourceOwnerAuthorizationUri(DiscogsAuthEndpoint.authorizeUri)
    )

  def getAccessToken(implicit signer: RequestTokenCredentials): F[
    Response[Either[ResponseError[Exception], AccessTokenCredentials]]
  ] = {

    implicit val responseMapping: ResponseMapping[String, Exception, AccessTokenCredentials] =
      AccessTokenCredentials.responseMapping(signer.consumer, signer.signatureMethod)

    baseRequest(client)
      .post(basePath / "access_token")
      .sign
      .response(mapInto[String, Exception, AccessTokenCredentials])
      .send()
  }

  def fromUri(resourceOwnerAuthorizationUriResponse: Uri,
              temporaryCredentials: TemporaryCredentials,
              signatureMethod: SignatureMethod = SignatureMethod.SHA1)(implicit f: Applicative[F]): F[
    Response[Either[ResponseError[Exception], AccessTokenCredentials]]
  ] =
    RequestTokenCredentials
      .fetchRequestTokenCredentials(resourceOwnerAuthorizationUriResponse, temporaryCredentials, signatureMethod)
      .fold(error => f.pure(Response(code = StatusCode.Unauthorized, body = Left(error))),
            requestTokenCredentials => getAccessToken(requestTokenCredentials))
}

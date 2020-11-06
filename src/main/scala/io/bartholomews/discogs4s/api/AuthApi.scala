package io.bartholomews.discogs4s.api

import cats.Applicative
import cats.effect.ConcurrentEffect
import io.bartholomews.discogs4s.endpoints.{AccessTokenEndpoint, AuthorizeUrl, Identity}
import io.bartholomews.discogs4s.entities.{RequestToken, UserIdentity}
import io.bartholomews.fsclient.client.FsClientV1
import io.bartholomews.fsclient.codecs.ResDecoder
import io.bartholomews.fsclient.entities.oauth._
import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.AccessTokenRequest
import io.bartholomews.fsclient.entities.{ErrorBodyString, FsResponse}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import org.http4s.client.oauth1.Token
import org.http4s.{Headers, Status, Uri}

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
class AuthApi[F[_]: ConcurrentEffect](client: FsClientV1[F, SignerV1]) {

  def getRequestToken(implicit signer: TemporaryCredentialsRequest): F[HttpResponse[RequestToken]] =
    AuthorizeUrl.runWith(client)

  def getAccessToken(implicit requestToken: RequestTokenCredentials): F[HttpResponse[AccessTokenCredentials]] = {

    implicit val decoderPipe: ResDecoder[String, AccessTokenCredentials] = {
      case s"oauth_token=$token&oauth_token_secret=$secret" =>
        Right(AccessTokenCredentials(Token(token, secret), requestToken.consumer))
      case other =>
        Left(new Exception(s"Unexpected response: [$other]"))
    }

    AccessTokenRequest(AccessTokenEndpoint.uri).runWith(client)
  }

  def fromUri(requestToken: RequestToken,
              callbackUri: Uri)(implicit f: Applicative[F]): F[HttpResponse[AccessTokenCredentials]] = {
    val queryParams = callbackUri.query.pairs
    val response: Either[String, String] = queryParams
      .collectFirst({
        case ("denied", Some(_)) => Left("permission_denied")
        case ("oauth_token", Some(token)) =>
          if (token != requestToken.token.value) Left("oauth_token_mismatch")
          else
            queryParams
              .collectFirst({
                case ("oauth_verifier", Some(verifier)) => Right(verifier)
              })
              .toRight("missing_oauth_verifier_query_parameter")
              .joinRight
      })
      .toRight("missing_required_query_parameters")
      .joinRight

    response.fold(
      errorMsg => f.pure(FsResponse(Headers.empty, Status.Unauthorized, Left(ErrorBodyString(errorMsg)))),
      verifier =>
        getAccessToken(
          RequestTokenCredentials(
            requestToken.token,
            verifier,
            client.appConfig.signer.consumer
          )
        )
    )
  }

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
  def me(implicit token: SignerV1): F[HttpResponse[UserIdentity]] = Identity.runWith(client)
}

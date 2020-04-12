package io.bartholomews.discogs4s.api

import cats.effect.Effect
import fs2.Pipe
import io.bartholomews.fsclient.client.effect.HttpEffectClient
import io.bartholomews.fsclient.entities.OAuthInfo.OAuthV1
import io.bartholomews.fsclient.entities.OAuthVersion.Version1._
import io.bartholomews.fsclient.entities._
import io.bartholomews.fsclient.requests.OAuthV1AuthorizationFramework.AccessTokenRequest
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.discogs4s.endpoints.{AccessTokenEndpoint, AuthorizeUrl, Identity}
import io.bartholomews.discogs4s.entities.{RequestToken, UserIdentity}
import org.http4s.client.oauth1.Token

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
class AuthApi[F[_]: Effect](client: HttpEffectClient[F, OAuthV1]) {

  import io.bartholomews.fsclient.implicits.{emptyEntityEncoder, plainTextDecoderPipe, rawJsonPipe, rawPlainTextPipe}

  def getRequestToken: F[HttpResponse[RequestToken]] =
    AuthorizeUrl.runWith(client)

  def getAccessToken(implicit requestToken: RequestTokenV1): F[HttpResponse[AccessTokenV1]] = {
    implicit val decoderPipe: Pipe[F, String, AccessTokenV1] = plainTextDecoderPipe({
      case Right(s"oauth_token=$token&oauth_token_secret=$secret") =>
        AccessTokenV1(Token(token, secret), requestToken.consumer)
    })
    AccessTokenRequest(AccessTokenEndpoint.uri).runWith(client)
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

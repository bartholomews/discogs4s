package io.bartholomews.discogs4s.api

import cats.effect.Effect
import fs2.Pipe
import fsclient.client.effect.HttpEffectClient
import fsclient.entities.OAuthInfo.OAuthV1
import fsclient.entities.OAuthVersion.V1
import fsclient.entities._
import fsclient.requests.AccessTokenRequestV1
import io.bartholomews.discogs4s.endpoints.{AccessTokenEndpoint, AuthorizeUrl, Identity}
import io.bartholomews.discogs4s.entities.{RequestTokenResponse, UserIdentity}
import org.http4s.client.oauth1.Token

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
class AuthApi[F[_]: Effect](client: HttpEffectClient[F, OAuthV1]) {

  import fsclient.implicits.{emptyEntityEncoder, plainTextDecoderPipe, rawJsonPipe, rawPlainTextPipe}

  def getRequestToken: F[HttpResponse[RequestTokenResponse]] =
    AuthorizeUrl.runWith(client)

  def getAccessToken(implicit requestToken: V1.RequestToken): F[HttpResponse[V1.AccessToken]] = {
    implicit val decoderPipe: Pipe[F, String, V1.AccessToken] = plainTextDecoderPipe({
      case Right(s"oauth_token=$token&oauth_token_secret=$secret") =>
        V1.AccessToken(Token(token, secret), requestToken.consumer)
    })
    AccessTokenRequestV1(AccessTokenEndpoint.uri).runWith[F, V1.type, OAuthV1](client)
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
  def me(implicit token: Signer[V1.type]): F[HttpResponse[UserIdentity]] = Identity.runWith(client)
}

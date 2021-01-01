package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod}
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.{AccessToken, RedirectUri}
import io.bartholomews.fsclient.core.oauth.{
  AuthDisabled,
  ClientCredentials,
  CustomAuthorizationHeader,
  OAuthSigner,
  Signer,
  SignerV1,
  TemporaryCredentialsRequest
}
import pureconfig.ConfigSource
import sttp.client.SttpBackend

sealed abstract class DiscogsAbstractClient[F[_], S <: Signer](
  implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
) {

  private[discogs4s] def client: FsClient[F, S]
  final object artists extends ArtistsApi[F, S](client)
  final object users extends UsersApi[F, S](client)
}

class DiscogsSimpleClient[F[_], S <: Signer](userAgent: UserAgent, signer: S)(
  implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
) extends DiscogsAbstractClient[F, S] {
  override private[discogs4s] val client = new FsClient[F, S](userAgent: UserAgent, signer: S, sttpBackend)
}

class DiscogsClient[F[_], S <: SignerV1](userAgent: UserAgent, signer: S)(
  implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
) extends DiscogsAbstractClient[F, S] {
  def temporaryCredentialsRequest(redirectUri: RedirectUri): TemporaryCredentialsRequest =
    TemporaryCredentialsRequest(signer.consumer, redirectUri)

  override private[discogs4s] val client = new FsClient[F, S](userAgent, signer, sttpBackend)
  final object auth extends AuthApi[F, S](client)
}

object DiscogsClient {

  import pureconfig.generic.auto._

  private val userAgentConfig = ConfigSource.default.at("user-agent")
  private val discogsConfig = ConfigSource.default.at("discogs")

  def basic[F[_]](userAgent: UserAgent)(
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ): DiscogsSimpleClient[F, AuthDisabled.type] = new DiscogsSimpleClient[F, AuthDisabled.type](
    userAgent,
    AuthDisabled
  )

  def basicFromConfig[F[_]](
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ): DiscogsSimpleClient[F, AuthDisabled.type] =
    basic(userAgentConfig.loadOrThrow[UserAgent])

  def personalToken(accessToken: AccessToken): CustomAuthorizationHeader =
    CustomAuthorizationHeader(s"Discogs token=${accessToken.value}")

  def personal[F[_]](userAgent: UserAgent, signer: CustomAuthorizationHeader)(
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ): DiscogsSimpleClient[F, OAuthSigner] = new DiscogsSimpleClient[F, OAuthSigner](
    userAgent,
    signer
  )

  def personalFromConfig[F[_]](
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ): DiscogsSimpleClient[F, OAuthSigner] =
    personal(
      userAgentConfig.loadOrThrow[UserAgent],
      personalToken(discogsConfig.at("access-token").loadOrThrow[AccessToken])
    )

  def clientCredentials[F[_]](userAgent: UserAgent, consumer: Consumer)(
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ) = new DiscogsClient[F, SignerV1](userAgent, ClientCredentials(consumer, SignatureMethod.PLAINTEXT))

  def clientCredentialsFromConfig[F[_]](
    implicit sttpBackend: SttpBackend[F, Nothing, Nothing]
  ): DiscogsClient[F, SignerV1] =
    clientCredentials(
      userAgentConfig.loadOrThrow[UserAgent],
      discogsConfig.at("consumer").loadOrThrow[Consumer]
    )
}

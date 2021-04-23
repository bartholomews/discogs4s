package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}
import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod}
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.AccessToken
import io.bartholomews.fsclient.core.oauth._
import pureconfig.ConfigSource
import sttp.client3.SttpBackend

sealed abstract class DiscogsAbstractClient[F[_], S <: Signer] {

  def client: FsClient[F, S]
  final object artists extends ArtistsApi[F, S](client)
  final object users extends UsersApi[F, S](client)
}

class DiscogsSimpleClient[F[_], S <: Signer](userAgent: UserAgent, signer: S)(backend: SttpBackend[F, Any])
    extends DiscogsAbstractClient[F, S] {
  override val client = new FsClient[F, S](userAgent: UserAgent, signer: S, backend)
}

class DiscogsClient[F[_], S <: SignerV1](userAgent: UserAgent, signer: S)(backend: SttpBackend[F, Any])
    extends DiscogsAbstractClient[F, S] {
  def temporaryCredentialsRequest(redirectUri: RedirectUri): TemporaryCredentialsRequest =
    TemporaryCredentialsRequest(signer.consumer, redirectUri)

  override val client = new FsClient[F, S](userAgent, signer, backend)
  final object auth extends AuthApi[F, S](client)
}

object DiscogsClient {

  import pureconfig.generic.auto._

  private val userAgentConfig = ConfigSource.default.at("user-agent")
  private val discogsConfig = ConfigSource.default.at("discogs")

  def basic[F[_]](userAgent: UserAgent)(backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, AuthDisabled.type] =
    new DiscogsSimpleClient[F, AuthDisabled.type](
      userAgent,
      AuthDisabled
    )(backend)

  def basicFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, AuthDisabled.type] =
    basic(userAgentConfig.loadOrThrow[UserAgent])(backend)

  def personalToken(accessToken: AccessToken): CustomAuthorizationHeader =
    CustomAuthorizationHeader(s"Discogs token=${accessToken.value}")

  def personal[F[_]](userAgent: UserAgent, signer: CustomAuthorizationHeader)(
    backend: SttpBackend[F, Any]
  ): DiscogsSimpleClient[F, OAuthSigner] =
    new DiscogsSimpleClient[F, OAuthSigner](
      userAgent,
      signer
    )(backend)

  def personalFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, OAuthSigner] =
    personal(
      userAgentConfig.loadOrThrow[UserAgent],
      personalToken(discogsConfig.at("access-token").loadOrThrow[AccessToken])
    )(backend)

  def clientCredentials[F[_]](userAgent: UserAgent, consumer: Consumer)(backend: SttpBackend[F, Any]) =
    new DiscogsClient[F, SignerV1](userAgent, ClientCredentials(consumer, SignatureMethod.PLAINTEXT))(backend)

  def clientCredentialsFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsClient[F, SignerV1] =
    clientCredentials(
      userAgentConfig.loadOrThrow[UserAgent],
      discogsConfig.at("consumer").loadOrThrow[Consumer]
    )(backend)
}

package io.bartholomews.discogs4s

import fsclient.client.io_client.IOAuthClient
import fsclient.config.FsClientConfig.{AppConfig, BasicAppConfig, TokenAppConfig}
import fsclient.config.{Derivations, UserAgent}
import fsclient.entities.OAuthVersion.V1
import fsclient.entities.SignerType.{OAuthV1AccessToken, OAuthV1BasicSignature}
import fsclient.entities.{OAuthVersion, Signer, SignerType}
import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}
import pureconfig.{ConfigReader, ConfigSource, Derivation}

import scala.concurrent.ExecutionContext

// https://http4s.org/v0.19/streaming/
class DiscogsClient(userAgent: UserAgent, signer: Signer[OAuthVersion.V1.type])(implicit ec: ExecutionContext)
    extends IOAuthClient[OAuthVersion.V1.type](userAgent, signer) {

  object auth extends AuthApi(this)
  object artists extends ArtistsApi(this)
  object users extends UsersApi(this)
}

object DiscogsClient {

  import pureconfig.generic.auto._

  private def discogsConfigKeyDerivation[C <: AppConfig](
    implicit reader: ConfigReader[C]
  ): Derivation[ConfigReader[C]] =
    Derivations.withCustomKey("discogs")

  def unsafeFromConfig(signerType: SignerType)(implicit ec: ExecutionContext): DiscogsClient = signerType match {
    case OAuthV1BasicSignature =>
      val consumerConfig =
        ConfigSource.default.load[BasicAppConfig](discogsConfigKeyDerivation).orThrow.consumer

      new DiscogsClient(consumerConfig.userAgent, V1.BasicSignature(consumerConfig.consumer))

    case OAuthV1AccessToken =>
      val config = ConfigSource.default.load[TokenAppConfig](discogsConfigKeyDerivation).orThrow
      val (consumerConfig, accessToken) = (config.consumer, config.accessToken)
      new DiscogsClient(
        userAgent = consumerConfig.userAgent,
        signer = V1.AccessToken(token = accessToken, consumer = consumerConfig.consumer)
      )
  }
}

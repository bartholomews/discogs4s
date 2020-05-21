package io.bartholomews.discogs4s

import cats.effect.{ContextShift, IO}
import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}
import io.bartholomews.fsclient.client.FsClientV1
import io.bartholomews.fsclient.config.{FsClientConfig, UserAgent}
import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
import io.bartholomews.fsclient.entities.oauth.{
  AccessTokenCredentials,
  ClientCredentials,
  SignerV1,
  TemporaryCredentialsRequest
}
import org.http4s.Uri
import org.http4s.client.oauth1.{Consumer, Token}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures

import scala.concurrent.ExecutionContext

// https://http4s.org/v0.19/streaming/
class DiscogsClient(userAgent: UserAgent, signer: SignerV1)(implicit ec: ExecutionContext) {
  implicit private val contextShift: ContextShift[IO] = IO.contextShift(ec)
  private val client = FsClientV1[IO, SignerV1](FsClientConfig.v1.basic(userAgent, signer.consumer))

  final def temporaryCredentialsRequest(callback: Uri): TemporaryCredentialsRequest =
    TemporaryCredentialsRequest(signer.consumer, callback)

  object auth extends AuthApi(client)
  object artists extends ArtistsApi(client)
  object users extends UsersApi(client)
}

object DiscogsClient {

  import io.bartholomews.fsclient.config.FsClientConfig.LoadConfigOrThrow
  import pureconfig.generic.auto._

  private val userAgentConfig = ConfigSource.default.at("user-agent")
  private val discogsConfig = ConfigSource.default.at("discogs")

  /**
   * Unsafely load userAgent and consumer from config
   * @param ec the `ExecutionContext` for the client runner
   * @return a fully configured `DiscogsClient` with the specified OAuth v1 signer type
   */
  def unsafeFromConfig(signerType: SignerType)(implicit ec: ExecutionContext): DiscogsClient = {
    val userAgent = userAgentConfig.load[UserAgent].orThrow
    val consumer = discogsConfig.at("consumer").load[Consumer].orThrow
    new DiscogsClient(
      userAgent,
      signerType match {
        case SignerType.BasicSignature => ClientCredentials(consumer)
        case SignerType.TokenSignature =>
          val accessToken = discogsConfig.at("access-token").load[Token].orThrow
          AccessTokenCredentials(token = accessToken, consumer)
      }
    )
  }

  /**
   * Unsafely load userAgent and consumer from config
   * @param ec the `ExecutionContext` for the client runner
   * @return a function which takes an OAuth v1 access token and returns a fully configured `DiscogsClient`
   */
  def unsafeFromConfig()(implicit ec: ExecutionContext): Token => DiscogsClient = {
    val userAgent = userAgentConfig.load[UserAgent].orThrow
    val consumer = discogsConfig.at("consumer").load[Consumer].orThrow
    accessToken =>
      new DiscogsClient(
        userAgent,
        AccessTokenCredentials(token = accessToken, consumer)
      )
  }

  /**
   * Safely load userAgent and consumer from config
   * @param ec the `ExecutionContext` for the client runner
   * @return Either the config reader failures
   *         or a function which takes an OAuth v1 access token and returns a fully configured `DiscogsClient`
   */
  def fromConfig(implicit ec: ExecutionContext): Either[ConfigReaderFailures, Token => DiscogsClient] =
    for {
      userAgent <- userAgentConfig.load[UserAgent]
      consumer <- discogsConfig.at("consumer").load[Consumer]
    } yield (accessToken: Token) =>
      new DiscogsClient(
        userAgent,
        AccessTokenCredentials(token = accessToken, consumer)
      )
}

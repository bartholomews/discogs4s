package io.bartholomews.discogs4s

import io.bartholomews.fsclient.core.FsClient
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth._
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod}
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.AccessToken
import pureconfig.ConfigReader.Result
import pureconfig.ConfigSource
import sttp.client3.SttpBackend

// https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow
object DiscogsClient {

  import pureconfig.generic.auto._

  private val userAgentConfig = ConfigSource.default.at("user-agent")
  private val discogsConfig = ConfigSource.default.at("discogs")

  /**
   * Credentials in request ? None
   * Rate limiting          ? 🐢 Low tier
   * Image URLs             ? ❌ No
   * Authenticated as user  ? ❌ No
   */
  object authDisabled {
    def apply[F[_]](userAgent: UserAgent)(backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, AuthDisabled.type] =
      new DiscogsSimpleClient[F, AuthDisabled.type](FsClient(userAgent, AuthDisabled, backend))

    def unsafeFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, AuthDisabled.type] =
      apply(userAgentConfig.loadOrThrow[UserAgent])(backend)
  }

  /**
   * Credentials in request ? Only Consumer key/secret
   * Rate limiting          ? 🐰 High tier
   * Image URLs             ? ✔ Yes
   * Authenticated as user  ? ❌ No
   */
  object clientCredentials {
    def apply[F[_]](userAgent: UserAgent, consumer: Consumer)(backend: SttpBackend[F, Any]) =
      new DiscogsSimpleClient[F, SignerV1](
        FsClient(userAgent, ClientCredentials(consumer, SignatureMethod.PLAINTEXT), backend)
      )

    def fromConfig[F[_]](backend: SttpBackend[F, Any]): Result[DiscogsSimpleClient[F, SignerV1]] =
      for {
        userAgent <- userAgentConfig.load[UserAgent]
        consumer <- discogsConfig.at("consumer").load[Consumer]
      } yield clientCredentials(userAgent, consumer)(backend)

    def unsafeFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsSimpleClient[F, SignerV1] =
      clientCredentials(
        userAgentConfig.loadOrThrow[UserAgent],
        discogsConfig.at("consumer").loadOrThrow[Consumer]
      )(backend)
  }

  /**
   * Credentials in request ? Personal access token
   * Rate limiting          ? 🐰 High tier
   * Image URLs             ? ✔ Yes
   * Authenticated as user  ? ✔ Yes, for token holder only 👩
   */
  object personal {
    private def personalToken(accessToken: AccessToken): CustomAuthorizationHeader =
      CustomAuthorizationHeader(s"Discogs token=${accessToken.value}")

    def apply[F[_]](userAgent: UserAgent, accessToken: AccessToken)(
      backend: SttpBackend[F, Any]
    ): DiscogsPersonalClient[F, OAuthSigner] =
      new DiscogsPersonalClient[F, OAuthSigner](FsClient(userAgent, personalToken(accessToken), backend))

    def fromConfig[F[_]](backend: SttpBackend[F, Any]): Result[DiscogsPersonalClient[F, OAuthSigner]] =
      for {
        userAgent <- userAgentConfig.load[UserAgent]
        accessToken <- discogsConfig.at("access-token").load[AccessToken]
      } yield personal(userAgent, accessToken)(backend)

    def unsafeFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsPersonalClient[F, OAuthSigner] =
      personal(
        userAgentConfig.loadOrThrow[UserAgent],
        discogsConfig.at("access-token").loadOrThrow[AccessToken]
      )(backend)
  }

  /**
   * Credentials in request ? Full OAuth 1.0a with access token/secret
   * Rate limiting          ? 🐰 High tier
   * Image URLs             ? ✔ Yes
   * Authenticated as user  ? ✔ Yes, on behalf of any user 🌍
   */
  object oAuth {
    def apply[F[_]](userAgent: UserAgent, consumer: Consumer)(backend: SttpBackend[F, Any]) =
      new DiscogsOAuthClient[F](userAgent, consumer)(backend)

    def fromConfig[F[_]](backend: SttpBackend[F, Any]): Result[DiscogsOAuthClient[F]] =
      for {
        userAgent <- userAgentConfig.load[UserAgent]
        consumer <- discogsConfig.at("consumer").load[Consumer]
      } yield oAuth(userAgent, consumer)(backend)

    def unsafeFromConfig[F[_]](backend: SttpBackend[F, Any]): DiscogsOAuthClient[F] =
      oAuth(
        userAgentConfig.loadOrThrow[UserAgent],
        discogsConfig.at("consumer").loadOrThrow[Consumer]
      )(backend)
  }
}

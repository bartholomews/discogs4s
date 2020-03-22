package io.bartholomews.discogs4s

import fsclient.client.io_client.IOAuthClient
import fsclient.config.FsClientConfig
import fsclient.entities.OAuthInfo.OAuthV1
import fsclient.entities.OAuthVersion.V1
import io.bartholomews.discogs4s.api.{ArtistsApi, AuthApi, UsersApi}

import scala.concurrent.ExecutionContext

// https://http4s.org/v0.19/streaming/
class DiscogsClient(val config: FsClientConfig[OAuthV1])(implicit ec: ExecutionContext) {

  def this()(implicit ec: ExecutionContext) = this(FsClientConfig.v1("discogs"))

  private val client =
    new IOAuthClient(config.userAgent, V1.BasicSignature(config.authInfo.signer.consumer))

  object auth extends AuthApi(client)
  object artists extends ArtistsApi(client)
  object users extends UsersApi(client)
}

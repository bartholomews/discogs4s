package io.bartholomews.discogs4s

import cats.effect.{ContextShift, IO, Resource}
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthEnabled
import fsclient.entities.AuthVersion.V1
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

trait MockClientConfig {

  import scala.concurrent.ExecutionContext

  // https://http4s.org/v0.20/client/
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  val sampleClient =
    new DiscogsClient(
      FsClientConfig(
        UserAgent("discogs-test", appVersion = None, appUrl = None),
        AuthEnabled(V1.BasicSignature(Consumer("key", "secret")))
      )
    )
}

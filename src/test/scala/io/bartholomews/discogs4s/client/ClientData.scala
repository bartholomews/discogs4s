package io.bartholomews.discogs4s.client

import cats.effect.{ContextShift, IO, Resource}
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.fsclient.config.UserAgent
import io.bartholomews.fsclient.entities.OAuthVersion.Version1.BasicSignature
import io.bartholomews.testudo.client.TestudoClientData
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object ClientData extends TestudoClientData {

  // https://http4s.org/v0.20/client/
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  val sampleClient =
    new DiscogsClient(
      UserAgent("discogs-test", appVersion = None, appUrl = None),
      BasicSignature(sampleConsumer)
    )

  case class DiscogsError(message: String)
}

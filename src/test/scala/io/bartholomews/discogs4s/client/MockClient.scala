package io.bartholomews.discogs4s.client

import cats.effect.{ContextShift, IO, Resource}
import fsclient.config.UserAgent
import fsclient.entities.OAuthVersion.V1
import io.bartholomews.discogs4s.DiscogsClient
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.{Consumer, Token}

import scala.concurrent.ExecutionContext

trait MockClient {

  // https://http4s.org/v0.20/client/
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  val sampleConsumer: Consumer =
    Consumer(key = "SAMPLE_CONSUMER_KEY", secret = "SAMPLE_CONSUMER_SECRET")

  val sampleToken: Token =
    Token(value = "SAMPLE_TOKEN_VALUE", secret = "SAMPLE_TOKEN_SECRET")

  val sampleClient =
    new DiscogsClient(
      UserAgent("discogs-test", appVersion = None, appUrl = None),
      V1.BasicSignature(sampleConsumer)
    )
}

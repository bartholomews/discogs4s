package client

import api.Identity
import cats.effect.{ContextShift, IO, Resource}
import client.http.{IOClient, OAuthClient}
import client.utils.Config.ConsumerConfig
import entities.AccessTokenResponse
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

import scala.concurrent.ExecutionContext

class DiscogsOAuthClient(consumerConfig: ConsumerConfig,
                         accessToken: AccessTokenResponse)(ec: ExecutionContext) extends DiscogsRest(consumerConfig) {

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource
  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  case object Me extends OAuthClient with IOClient[String] {
    def apply(): IOResponse[String] =
      resource.use(fetchJson(_)(getRequest(Identity.uri), Some(accessToken)))
  }

}
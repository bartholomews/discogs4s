package client

import api.Identity
import cats.effect.{IO, Resource}
import client.http.{IOClient, OAuthClient}
import client.utils.Config.DiscogsConsumer
import entities.{AccessTokenResponse, UserIdentity}
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

private[client] class DiscogsOAuthClient(consumerConfig: DiscogsConsumer, accessToken: AccessTokenResponse)
                                        (implicit resource: Resource[IO, Client[IO]]) extends DiscogsRest(consumerConfig) {

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  case object Me extends OAuthClient with IOClient[UserIdentity] {
    def apply(): IOResponse[UserIdentity] =
      resource.use(fetchJson(_)(getRequest(Identity.uri), Some(accessToken)))
  }

}
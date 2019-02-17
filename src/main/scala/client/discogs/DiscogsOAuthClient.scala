package client.discogs

import cats.effect.{IO, Resource}
import client.discogs.api.Identity
import client.discogs.entities.{AccessTokenResponse, UserIdentity}
import client.discogs.utils.Config.DiscogsConsumer
import client.effect4s.IOClient
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

private[client] class DiscogsOAuthClient(consumerConfig: DiscogsConsumer, accessToken: AccessTokenResponse)
                                        (implicit resource: Resource[IO, Client[IO]])

  extends DiscogsRest(consumerConfig)
    with IOClient
    with DiscogsOAuthPipes {

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  case object Me {
    def apply(): IOResponse[UserIdentity] =
      resource.use(fetchJson(_)(getRequest(Identity.uri), Some(accessToken)))
  }
}
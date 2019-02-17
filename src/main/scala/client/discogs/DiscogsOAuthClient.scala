package client.discogs

import cats.effect.{IO, Resource}
import client.discogs.api.{DiscogsEndpoint, Identity}
import client.discogs.entities.{AccessTokenResponse, DiscogsEntity, UserIdentity}
import client.effect4s.IOClient
import client.effect4s.config.OAuthConsumer
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

private[client] class DiscogsOAuthClient(consumerConfig: OAuthConsumer,
                                         accessToken: AccessTokenResponse)
                                        (implicit resource: Resource[IO, Client[IO]])
  extends IOClient(consumerConfig)
    with DiscogsOAuthPipes {

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private case class GET[T <: DiscogsEntity](private val endpoint: DiscogsEndpoint[T])(implicit decode: Decoder[T]) {
    def io: IOResponse[T] = fetchJson(endpoint.uri, accessToken = Some(accessToken))
  }

  case object Me {
    def apply(): IOResponse[UserIdentity] = GET[UserIdentity](Identity).io
  }

}
package client

import api.Identity
import client.http.{IOClient, OAuthClient}
import client.utils.ConsumerConfig
import entities.AccessTokenResponse

import scala.concurrent.ExecutionContext

class DiscogsOAuthClient(consumerClient: Option[ConsumerConfig] = None,
                         accessToken: AccessTokenResponse)(ec: ExecutionContext) extends DiscogsClient(consumerClient)(ec) {

  case object Me extends OAuthClient with IOClient[String] {
    def apply(): IOResponse[String] =
      resource.use(fetchJson(_)(getRequest(Identity.uri), Some(accessToken)))
  }
}
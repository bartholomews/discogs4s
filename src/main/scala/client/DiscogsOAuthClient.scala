package client

import api.Identity
import client.http.{IOClient, OAuthClient}
import client.utils.ConsumerConfig
import entities.AccessTokenResponse

class DiscogsOAuthClient(consumerClient: Option[ConsumerConfig] = None,
                         accessToken: AccessTokenResponse) extends DiscogsClient(consumerClient) {

  case object Me extends OAuthClient with IOClient[String] {
    def apply(): IOResponse[String] =
      fetchJson(getRequest(Identity.uri), Some(accessToken))
  }

}

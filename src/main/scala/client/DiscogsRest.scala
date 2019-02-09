package client

import cats.effect.Effect
import client.utils.Config.DiscogsConsumer
import org.http4s.{Header, Headers, Method, Request, Uri}

class DiscogsRest(consumerConfig: DiscogsConsumer) {

//  self: ConsumerConfig =>

  private[client] val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private[client] def request[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  private[client] def getRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.GET)

  private[client] def postRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.POST)
}

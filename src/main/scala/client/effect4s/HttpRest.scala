package client.effect4s

import cats.effect.Effect
import client.discogs.utils.Config.DiscogsConsumer
import org.http4s.{Header, Headers, Method, Request, Uri}

// TODO: try to move this to `effect4s`, that is taking a `HttpConfig` generic Confif type instead of `DiscogsConsumer`
class HttpRest(consumerConfig: DiscogsConsumer) {

//  self: ConsumerConfig =>

  val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  def request[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  def getRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.GET)

  def postRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.POST)
}
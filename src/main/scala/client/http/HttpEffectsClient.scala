package client.http

import api.{AccessTokenRequest, OAuthAccessToken}
import cats.effect.Effect
import io.circe.Decoder
import org.http4s.Request
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

trait HttpEffectsClient[F[_], T] extends RequestF[T] {

  def run: fs2.Stream[F, HttpResponse[T]] => F[HttpResponse[T]]

  def pipeTransform: PipeTransform[F, String, T]

  private[client] def fetchPlainText(client: Client[F])(request: Request[F], accessTokenRequest: Option[AccessTokenRequest] = None)
                                    (implicit effect: Effect[F], consumer: Consumer): F[HttpResponse[T]] =

    run(plainTextRequest[F](client)(withLogger(request), accessTokenRequest)(pipeTransform)(effect, consumer))

  private[client] def fetchJson(client: Client[F])(request: Request[F], token: Option[OAuthAccessToken] = None)
                               (implicit effect: Effect[F],
                                consumer: Consumer,
                                decode: Decoder[T]): F[HttpResponse[T]] =

    run(jsonRequest(client)(withLogger(request), token))
}

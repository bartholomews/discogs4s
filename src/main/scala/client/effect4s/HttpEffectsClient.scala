package client.effect4s

import client.discogs.api.AccessTokenRequest
import cats.effect.Effect
import client.effect4s.entities.{HttpResponse, OAuthAccessToken}
import io.circe.Decoder
import org.http4s.Request
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

trait HttpEffectsClient[F[_]] extends RequestF {

  def run[A]: fs2.Stream[F, HttpResponse[A]] => F[HttpResponse[A]]

  def fetchPlainText[A](client: Client[F])(request: Request[F], accessTokenRequest: Option[AccessTokenRequest] = None)
                                         (implicit
                                          effect: Effect[F],
                                          consumer: Consumer,
                                          plainTextToDomainPipe: HttpPipe[F, String, A]): F[HttpResponse[A]] =

    run(plainTextRequest[F, A](client)(request, accessTokenRequest))


  def fetchJson[A](client: Client[F])(request: Request[F], token: Option[OAuthAccessToken] = None)
                                    (implicit effect: Effect[F],
                                     consumer: Consumer,
                                     decode: Decoder[A]): F[HttpResponse[A]] =

    run(jsonRequest(client)(request, token))
}

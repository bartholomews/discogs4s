package client.effect4s

import cats.effect.{Effect, Resource}
import client.effect4s.config.OAuthConsumer
import client.effect4s.entities.{HttpResponse, OAuthAccessToken}
import io.circe.{Decoder, Json}
import org.http4s.client.oauth1.Consumer
import org.http4s.{EntityEncoder, Header, Headers, Method, Request, Uri}

trait HttpEffectClient[F[_]] extends RequestF {

  val oAuthConsumer: OAuthConsumer

  val USER_AGENT = Headers {
    Header("User-Agent", oAuthConsumer.userAgent)
  }

  import io.circe.syntax._
  import org.http4s.client._
  //  import org.http4s.circe.CirceEntityEncoder._
  //  import org.http4s.circe._
  //
  //  import org.http4s.circe.CirceEntityEncoder
  //  import org.http4s.circe.CirceEntityDecoder._
  //  import org.http4s.circe.CirceEntityCodec._
  //  import org.http4s.circe.CirceInstances._
  //
  //  import io.circe.generic.auto._

  //  implicit val body = jsonEncoderOf[F, Json]

  //  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A],
  //                                   applicative: Applicative[F]): EntityEncoder[F, A] =
  //    org.http4s.circe.jsonEncoderOf[F, A]

  private def request(uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  private def POST[A](uri: Uri)
                     (implicit entityEncoder: EntityEncoder[F, Json]): Request[F] =
    request(uri).withMethod(Method.POST).withEntity[Json]("dsd".asJson)

  def run[A]: fs2.Stream[F, HttpResponse[A]] => F[HttpResponse[A]]

  def fetchPlainTextWithBody[A, B](uri: Uri,
                                   method: Method = Method.POST,
                                   body: B,
                                   accessToken: Option[OAuthAccessToken] = None)
                                  (implicit
                                   effect: Effect[F],
                                   consumer: Consumer,
                                   resource: Resource[F, Client[F]],
                                   bodyEntityEncoder: EntityEncoder[F, B],
                                   plainTextToEntityPipe: HttpPipe[F, String, A]): F[HttpResponse[A]] =

    resource.use(client => run(
      plainTextRequest[F, A](client)(request(uri)
        .withMethod(method).withEntity(body), accessToken)
    ))

  def fetchPlainText[A](uri: Uri,
                        method: Method = Method.GET,
                        accessToken: Option[OAuthAccessToken] = None)
                       (implicit
                        effect: Effect[F],
                        consumer: Consumer,
                        resource: Resource[F, Client[F]],
                        plainTextToEntityPipe: HttpPipe[F, String, A]): F[HttpResponse[A]] =

    resource.use(client => run(
      plainTextRequest[F, A](client)(request(uri)
        .withMethod(method), accessToken)
    ))

  def fetchJsonWithBody[A, B](uri: Uri,
                              method: Method = Method.POST,
                              body: Option[B] = None,
                              accessToken: Option[OAuthAccessToken] = None)
                             (implicit
                              effect: Effect[F],
                              consumer: Consumer,
                              resource: Resource[F, Client[F]],
                              bodyEntityEncoder: EntityEncoder[F, B],
                              decode: Decoder[A]): F[HttpResponse[A]] =

    resource.use(client => run(
      jsonRequest(client)(request(uri)
        .withMethod(method)
        .withEntity(body.get), accessToken)
    ))

  def fetchJson[A](uri: Uri,
                   method: Method = Method.GET,
                   accessToken: Option[OAuthAccessToken] = None)
                  (implicit
                   effect: Effect[F],
                   consumer: Consumer,
                   resource: Resource[F, Client[F]],
                   decode: Decoder[A]): F[HttpResponse[A]] =

    resource.use(client => run(
      jsonRequest(client)(request(uri)
        .withMethod(method), accessToken)))
}
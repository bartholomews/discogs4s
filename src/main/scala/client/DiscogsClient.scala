package client

import cats.effect.{Effect, IO}
import client.api.{AccessTokenRequest, AuthorizeUrl, DiscogsApi, OAuthResponse}
import entities.DiscogsEntity
import io.circe.Decoder
import org.http4s.client.oauth1.Consumer
import org.http4s.{Header, Headers, Method, Request, Uri}
import utils.{Config, ConsumerConfig, Types}

import scala.util.{Failure, Success, Try}

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication
case class DiscogsClient(consumerClient: Option[ConsumerConfig] = None) extends Types {

  private val consumerConfig = consumerClient.getOrElse(Config.CONSUMER_CONFIG) // todo handle error
  private implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private def request[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  private def post[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.POST)

  private def get[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.GET)

  case class GET[T <: DiscogsEntity](private val api: DiscogsApi[T])
                                    (implicit decode: Decoder[T]) extends IOClient[T] {

    def io: IO[T] = fetchJson(get(api.uri))

    def ioEither: IO[Either[Throwable, T]] = io.attempt

    def ioTry: IO[Try[T]] = ioEither.map(_.fold(
      throwable => Failure(throwable),
      response => Success(response)
    ))
  }

  case object OAUTH extends OAuthClient {

    def getAuthoriseUrl: IOResponse[OAuthResponse] =
      fetchPlainText(get(AuthorizeUrl.uri))

    def accessToken(request: AccessTokenRequest): IOResponse[OAuthResponse] =
      fetchPlainText(post(request.uri), Some(request))
  }

}
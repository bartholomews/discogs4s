package client

import cats.effect.{Effect, IO}
import client.api.{AuthorizeUrl, DiscogsApi}
import entities.DiscogsEntity
import io.circe.Decoder
import org.http4s.client.oauth1.Consumer
import org.http4s.{Header, Headers, Method, Request, Uri}
import utils.{Config, ConsumerConfig, Logger, Types}

import scala.util.{Failure, Success, Try}

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication
case class DiscogsClient(consumerClient: Option[ConsumerConfig] = None) extends Types with Logger {

  private val consumerConfig = consumerClient.getOrElse(Config.CONSUMER_CONFIG) // todo handle error
  private implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private def get[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withMethod(Method.GET)
    .withUri(uri)
    .withHeaders(USER_AGENT)

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

    def getAuthoriseUrl: IOResponse[Uri] = {
      plainTextRequest[IO](withLogger(get(AuthorizeUrl.uri)))(
        plainTextToUriResponse)
        .compile
        .last
        .flatMap(_.toRight(emptyResponse).fold(
          empty => IO.raiseError(empty),
          value => IO.pure(value)
        ))
    }

//    def accessTokenRequest: IOResponse[Token] = {
//
//    }

  }

}
package client

import cats.effect.{Effect, IO}
import fs2.Stream
import io.circe.{Decoder, Json}
import io.circe.fs2._
import org.http4s.client.blaze.Http1Client
import org.http4s.{Method, Request, Status, Uri}
import utils.Logger

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

case class ResponseError() extends Throwable {
  override val getMessage = "Some error indeed"
}

// https://http4s.org/v0.19/streaming/
case class DiscogsClient() extends ArtistsApi with Logger {

  //TODO User-Agent
  // $APP_NAME/$APP_VERSION +$APP_URL
  // e.g. Bidwish/1.0 (+https://github.com/bartholomews/bidwish)

  def fetch[F[_] : Effect, T](request: Request[F])
                             (implicit decode: Decoder[T]): Stream[F, Either[ResponseError, T]] = {
    val stream = Http1Client.stream[F]()

    val statusStream: Stream[F, Status] = stream.evalMap(c => c.status(request))

    // TODO Pipe trying to handle each either case, would be good to be able to extract Status

    val jsonStream = stream
      .flatMap(_.streaming(request)(resp => resp.body))
      .through(byteStreamParser)
      .through(jsonBodyLogger)

    jsonStream
      .through(decoder[F, T])
      .attempt
      .map(either => either.left.map(a => ResponseError()))
  }

  def requestIO[T](maybeUri: Try[Uri], method: Method)
                  (implicit decode: Decoder[T]): IO[T] = {
    maybeUri match {
      case Success(uri) =>
        fetch[IO, T](Log(Request[IO](method, uri)))
          .evalMap(IO.fromEither(_))
          .compile
          .toList
          .map(_.head) // FIXME exception head of empty list :(
      case Failure(ex) => LogError(IO.raiseError(ex))
    }
  }

  def GET[T](maybeUri: Try[Uri])(implicit decoder: Decoder[T]): IO[T] = {
    requestIO[T](maybeUri, Method.GET)
  }
}

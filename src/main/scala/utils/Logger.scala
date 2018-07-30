package utils

import cats.effect.Effect
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.fs2.decoder
import org.http4s.{Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger {

  private[this] val logger = getLogger("discogs4s").logger

  logger.info(s"$logger started.")

  type ThrowableOr[T] = Either[Throwable, T]

  def jsonBodyLogger[F[_] : Effect]: Pipe[F, Json, Json] =
    stream => {
      stream
        .through(decoder[F, Json])
        .map(json => debug(json))
    }

  def withLogger[F[_] : Effect](res: Stream[F, ThrowableOr[String]]): Stream[F, ThrowableOr[String]] = {
    res.map(debug)
  }

  def withLogger[F[_] : Effect, T](f: Response[F] => Stream[F, ThrowableOr[T]])
                                  (res: Response[F]): Stream[F, ThrowableOr[T]] = {

    val headers = res.headers.mkString("\n\t")
    val message = s"{\n\t${res.status}\n\t$headers\n}"
    logger.debug(message)
    f(res)
  }

  def withLogger[F[_] : Effect](request: Request[F]): Request[F] = {
    logger.info(s"${request.method.name} REQUEST: ${request.uri}")
    logger.info(s"${request.headers.map(_.toString())}")
    request
  }

  private def debug(either: ThrowableOr[String]): ThrowableOr[String] = {
    either.fold(
      throwable => logger.debug(throwable.getMessage),
      str => logger.debug(str)
    )
    either
  }

  private def debug(json: Json): Json = {
    logger.debug(json.toString)
    json
  }

}

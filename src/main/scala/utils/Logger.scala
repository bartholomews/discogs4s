package utils

import cats.effect.Effect
import entities.ResponseError
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.fs2.decoder
import org.http4s.{Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger {

  private[this] val logger = getLogger("discogs4s").logger

  logger.info(s"$logger started.")

  type ErrorOr[T] = Either[ResponseError, T]

  def jsonLogPipe[F[_] : Effect]: Pipe[F, Json, Json] = _.map(debug)

  def withLogger[F[_] : Effect](res: Stream[F, ErrorOr[String]]): Stream[F, ErrorOr[String]] = {
    res.map(debug)
  }

  def withLogger[F[_] : Effect, T](f: Response[F] => Stream[F, ErrorOr[T]])
                                  (res: Response[F]): Stream[F, ErrorOr[T]] = {

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

  def logError(throwable: Throwable): Throwable = {
    logger.error(throwable.getMessage, throwable)
    throwable
  }

  private def debug(either: ErrorOr[String]): ErrorOr[String] = {
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

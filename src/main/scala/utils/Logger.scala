package utils

import cats.effect.{Effect, IO}
import fs2.Pipe
import io.circe.Json
import io.circe.fs2.decoder
import org.http4s.{EntityBody, Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger {

  private[this] val logger = getLogger("discogs4s").logger

  logger.info(s"$logger started.")

  final def jsonBodyLogger[F[_]]: Pipe[F, Json, Json] =
    stream => {
      stream
        .through(decoder[F, Json])
        .map(json => debug(json))
    }

  private def debug(json: Json): Json = {
    logger.debug(json.toString)
    json
  }

  def Log[F[_] : Effect](request: Request[F]): Request[F] = {
    logger.info(s"${request.method.name} REQUEST: ${request.uri}")
    request
  }

  def LogError(io: IO[Nothing]): IO[Nothing] = {
    io.attempt.flatMap(either => {
      either.left.map(ex => logger.error(ex.toString))
      io
    })
  }

}

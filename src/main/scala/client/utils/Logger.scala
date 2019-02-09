package client.utils

import cats.Show
import cats.effect.Effect
import fs2.Pipe
import io.circe.Json
import org.http4s.{Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger extends HttpTypes {

  private[this] val logger = getLogger("discogs4s").logger

  logger.info(s"$logger started.")

  private[client] def jsonLogPipe[F[_] : Effect]: Pipe[F, Json, Json] = _.map(entity => {
    logger.debug(entity.toString)
    entity
  })

  private[client] def plainTextResponseLogPipe[F[_] : Effect]: Pipe[F, ErrorOr[String], ErrorOr[String]] =
    _.map(response => {
      logger.debug(s"RESPONSE:\n${response.fold(_.toString, _.toString)}")
      response
    })

  private[client] def logRequestHeaders[F[_] : Effect](request: Request[F]): Request[F] = {
    logger.info(s"${request.method.name} REQUEST: [${request.uri}]")
    logger.info(s"${request.headers.map(_.toString())}")
    request
  }

  private[client] def logResponseHeaders[F[_] : Effect, T](res: Response[F]): Response[F] = {
    val headers = res.headers.mkString("\n\t")
    val message = s"{\n\t${res.status}\n\t$headers\n}"
    logger.debug(message)
    res
  }

//  TODO log once in pipe which create an instance of `ResponseError`
//  def logError(throwable: Throwable): Throwable = {
//    logger.error(throwable.getMessage)
//    throwable
//  }

}
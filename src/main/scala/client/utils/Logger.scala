package client.utils

import cats.effect.Effect
import client.http.HttpResponse
import fs2.{Pipe, Stream}
import io.circe.Json
import org.http4s.{Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger extends HttpTypes {

  private[this] val logger = getLogger("discogs").logger

  logger.info(s"$logger started.")

  private[client] def jsonLogPipe[F[_] : Effect]: Pipe[F, Json, Json] = _.map(debug)

  private[client] def withLogger[F[_] : Effect, T](f: Response[F] => Stream[F, HttpResponse[T]])
                                  (res: Response[F]): Stream[F, HttpResponse[T]] = {

    val headers = res.headers.mkString("\n\t")
    val message = s"{\n\t${res.status}\n\t$headers\n}"
    logger.debug(message)
    f(res)
  }

  private[client] def withLogger[F[_] : Effect](request: Request[F]): Request[F] = {
    logger.info(s"${request.method.name} REQUEST: ${request.uri}")
    logger.info(s"${request.headers.map(_.toString())}")
    request
  }

  private def debug(json: Json): Json = {
    logger.debug(json.toString)
    json
  }

}
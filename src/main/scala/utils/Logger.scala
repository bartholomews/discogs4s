package utils

import cats.effect.Effect
import client.HttpResponse
import fs2.{Pipe, Stream}
import io.circe.Json
import org.http4s.{Request, Response}
import org.log4s.getLogger

import scala.language.higherKinds

trait Logger extends HttpResponseUtils {

  private[this] val logger = getLogger("discogs4s").logger

  logger.info(s"$logger started.")

  def jsonLogPipe[F[_] : Effect]: Pipe[F, Json, Json] = _.map(debug)

  def withLogger[F[_] : Effect, T](f: Response[F] => Stream[F, HttpResponse[T]])
                                  (res: Response[F]): Stream[F, HttpResponse[T]] = {

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

  private def debug(json: Json): Json = {
    logger.debug(json.toString)
    json
  }

}
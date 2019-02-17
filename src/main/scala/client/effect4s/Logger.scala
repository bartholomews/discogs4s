package client.effect4s

import cats.effect.Effect
import fs2.Pipe
import org.http4s.{Request, Response}
import org.log4s.getLogger

trait Logger extends HttpTypes {

  import pureconfig.generic.auto._

  private[effect4s] case class LoggerConfig(logger: Logger)
  private[effect4s] case class Logger(name: String)

  val loggerName: String = pureconfig
    .loadConfig[LoggerConfig]
    .map(_.logger.name)
    .getOrElse("http-effect4s-logger")

  private[this] val logger = getLogger(loggerName).logger

  logger.info(s"$logger started.")

  private[client] def logRequestHeaders[F[_] : Effect](request: Request[F]): Request[F] = {
    logger.info(s"${request.method.name} REQUEST: [${request.uri}]")
    logger.info(s"${request.headers.map(_.toString())}")
    request
  }

  private[client] def responseLogPipe[F[_] : Effect, T]: Pipe[F, T, T] = _.map(entity => {
    logger.debug(s"RESPONSE:\n${entity.toString}")
    entity
  })

  // TODO: consider `Show` instead of `toString`
  private[client] def errorLogPipe[T, F[_] : Effect]: Pipe[F, T, T] = _.map(entity => {
    logger.error(entity.toString)
    entity
  })

  private[client] def logResponseHeaders[F[_] : Effect, T](res: Response[F]): Response[F] = {
    val headers = res.headers.mkString("\n\t")
    val message = s"{\n\t${res.status}\n\t$headers\n}"
    logger.debug(message)
    res
  }
}

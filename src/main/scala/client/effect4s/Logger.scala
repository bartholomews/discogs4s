package client.effect4s

import cats.effect.Effect
import fs2.Pipe
import org.http4s.{Request, Response}
import org.log4s.getLogger
import cats.implicits._

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

  private[effect4s] def requestHeadersLogPipe[F[_] : Effect]: Pipe[F, Request[F], Request[F]] =
    _.map(request => {
      logger.info(s"${request.method.name} REQUEST: [${request.uri}]")
      logger.info(s"${request.headers.map(_.toString())}")
      request
    })

  private[effect4s] def responseHeadersLogPipe[F[_] : Effect, T]: Pipe[F, Response[F], Response[F]] =
    _.map(res => {
      val headers = res.headers.mkString("\n\t")
      val message = s"{\n\t${res.status}\n\t$headers\n}"
      logger.debug(message)
      res
    })

  private[effect4s] def responseLogPipe[F[_] : Effect, A]: Pipe[F, ErrorOr[A], ErrorOr[A]] =
    _.map(entity => {
      logger.debug(s"RESPONSE:\n$entity")
      entity
    })

  private[effect4s] def errorLogPipe[F[_] : Effect, A]: Pipe[F, Either[Throwable, A], Either[Throwable, A]] =
    _.map(_.leftMap(throwable => {
      logger.error(throwable.getMessage)
      throwable
    }))
}

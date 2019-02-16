package client.http

import api.{AccessTokenRequest, OAuthAccessToken}
import cats.effect.Effect
import cats.implicits._
import client.utils.{HttpTypes, Logger}
import entities.ResponseError
import fs2.{Pipe, Pure, Stream}
import io.circe.Decoder
import io.circe.fs2.{byteStreamParser, decoder}
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer
import org.http4s.headers.`Content-Type`
import org.http4s.{Headers, Request, Response, Status}

// TODO remove discogs dependencies so it could be moved to a different more general http module/library
trait RequestF[T] extends HttpTypes with Logger {

  private[client] def jsonRequest[F[_] : Effect](client: Client[F])(request: Request[F], accessToken: Option[OAuthAccessToken] = None)
                                                (implicit
                                                 consumer: Consumer,
                                                 decoder: Decoder[T]): Stream[F, HttpResponse[T]] =

    fetchResponse(client)(request, accessToken)(response => {
      response.status match {
        case Status.Ok =>
          Stream.emit(response)
            .through(jsonResponseDecoder)
            .through(responseLogPipe)

        case _ =>
          Stream.emit(response)
            .through(errorHandler)
            .through(responseLogPipe)
      }
    })

  private[client] def plainTextRequest[F[_] : Effect](client: Client[F])(request: Request[F], accessTokenRequest: Option[AccessTokenRequest] = None)
                                                     (plainTextResponseDecoder: PipeTransform[F, String, T])
                                                     (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    fetchResponse(client)(request, accessTokenRequest)(response =>

      response.status match {

        case Status.Ok =>
          Stream.eval(response.as[String])
            .attempt
            .through(responseErrorPipe(Status.UnprocessableEntity))
            .through(responseLogPipe)
            .through(plainTextResponseDecoder)

        case _ =>
          Stream.emit(response)
            .through(errorHandler)
            .through(responseLogPipe)
            .through(plainTextResponseDecoder)
      })
  }

  // TODO log.ERROR

  private def errorHandler[F[_] : Effect]: Pipe[F, Response[F], ErrorOr[Nothing]] = _.flatMap(
    response => {
      response.headers.get(`Content-Type`).map(_.value) match {
        case Some("application/json") =>
          response
            .body
            .through(byteStreamParser)
            .attempt
            .through(responseErrorLeftPipe(response.status, _.spaces2))

        case Some("text/plain") => Stream.eval(response.as[String])
          .attempt
          .through(responseErrorLeftPipe(response.status))

        case Some(unexpectedContentType) => Stream.emit(ResponseError(
          new Exception(s"$unexpectedContentType: unexpected `Content-Type`"), Status.UnsupportedMediaType).asLeft
        )
        case None =>
          Stream.emit(ResponseError(new Exception("`Content-Type` not provided"), Status.UnsupportedMediaType).asLeft)
      }
    }
  )

  private def jsonResponseDecoder[F[_] : Effect](implicit decode: Decoder[T]): Pipe[F, Response[F], ErrorOr[T]] = _.flatMap(
    _.body
      .through(byteStreamParser)
      .through(decoder[F, T])
      .attempt
      .through(responseErrorPipe(Status.UnprocessableEntity))
  )

  private def fetchResponse[F[_] : Effect](client: Client[F])(request: Request[F], accessToken: Option[OAuthAccessToken] = None)
                                          (f: Response[F] => Stream[F, ErrorOr[T]])
                                          (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer, accessToken)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      request <- signed
      response <- client.stream(logRequestHeaders(request))
      httpRes <- f(logResponseHeaders(response)).map(HttpResponse(response.headers, _))
    } yield httpRes
  }

  // -------------------------------------------------------------------------------------------------------------------

  private[client] def responseErrorPipe[F[_] : Effect, A](status: Status): Pipe[F, Either[Throwable, A], ErrorOr[A]] =
    _.map(
      _.fold(
        err => Left(ResponseError(err, status)),
        res => Right(res)
      )
    )

  private[client] def responseErrorLeftPipe[A, F[_] : Effect]
  (status: Status, f: A => String = (res: A) => res.toString): Pipe[F, Either[Throwable, A], ErrorOr[Nothing]] =
    _.map(_.fold(
      err => ResponseError(err, status).asLeft,
      res => ResponseError(new Exception(f(res)), status).asLeft
    ))

  // -------------------------------------------------------------------------------------------------------------------

  import org.http4s.client.oauth1._

  private def sign[F[_] : Effect](consumer: Consumer, accessToken: Option[OAuthAccessToken] = None)
                                 (req: Request[F]): F[Request[F]] = {
    signRequest(
      req,
      consumer,
      callback = None,
      verifier = accessToken.flatMap(_.verifier),
      accessToken.map(_.token)
    )
  }
}

case class HttpResponse[T](headers: Headers,
                           entity: Either[ResponseError, T]) {

  val status: Status = entity.fold(_.status, _ => Status.Ok)
}

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

    fetchResponse(client)(request, accessToken)(res => {
      res.status match {
        case Status.Ok =>
          Stream.emit(res)
            .through(jsonResponseDecoder)

        case _ =>
          Stream.emit(res)
            .through(errorHandler)
      }
    })

  private[client] def plainTextRequest[F[_] : Effect](client: Client[F])(request: Request[F], accessTokenRequest: Option[AccessTokenRequest] = None)
                                                     (plainTextResponseDecoder: PipeTransform[F, String, T])
                                                     (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    fetchResponse(client)(request, accessTokenRequest)(res =>
      Stream.eval(res.as[String])
        .attempt
        .through(errorPipe(res.status))
        .through(plainTextResponseLogPipe)
        .through(plainTextResponseDecoder)
    )
  }

  // TODO try with .through pipes instead of composed functions, create and log `ErrorResponse.apply` once
  //    Stream.eval(fetchResponse(client)(request, accessToken))
  //      .through(parseJsonPipe)
  //    (res => validateContentType(parseJson[F])(res)
  //    )

  private def errorHandler[F[_] : Effect]: Pipe[F, Response[F], ErrorOr[T]] = _.flatMap(
    response => {
      response.headers.get(`Content-Type`).map(_.value) match {
        case Some("application/json") =>
          response
            .body
            .through(byteStreamParser)
            .through(jsonLogPipe)
            .attempt
            .map(_.fold(
              err => ResponseError(new Exception(err.getMessage), response.status).asLeft[T],
              json => ResponseError(new Exception(json.spaces2), response.status).asLeft[T])
            )
        case Some("text/plain") => Stream.eval(response.as[String])
          .attempt
          .map(_.fold(
            err => ResponseError(new Exception(err.getMessage), response.status).asLeft[T],
            text => ResponseError(new Exception(text), response.status).asLeft[T])
          )
        case Some(unexpectedContentType) => Stream.emit(ResponseError(
          new Exception(s"$unexpectedContentType: unexpected `Content-Type`"), Status.UnsupportedMediaType).asLeft[T]
        )
        case None =>
          Stream.emit(ResponseError(new Exception("`Content-Type` not provided"), Status.UnsupportedMediaType).asLeft[T])
      }
    }
  )

  private def jsonResponseDecoder[F[_] : Effect](implicit decode: Decoder[T]): Pipe[F, Response[F], ErrorOr[T]] = _.flatMap(
    _.body
      .through(byteStreamParser)
      .through(jsonLogPipe)
      .through(decoder[F, T])
      .attempt
      .map(_.leftMap(ResponseError(_)))
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

  private def errorPipe[F[_], U](status: Status): Pipe[F, Either[Throwable, U], ErrorOr[U]] =
    _.map {

      case Right(res) =>
        if (status == Status.Ok) Right(res)
        else Left(ResponseError(new Exception(res.toString), Status.BadRequest))

      case Left(throwable) => Left(ResponseError(throwable, status))
    }

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

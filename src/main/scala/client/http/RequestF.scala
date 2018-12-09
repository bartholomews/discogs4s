package client.http

import api.{AccessTokenRequest, OAuthAccessToken}
import cats.effect.Effect
import client.utils.{HttpTypes, Logger}
import entities.ResponseError
import fs2.{Pipe, Pure, Stream}
import io.circe.fs2.{byteStreamParser, decoder}
import io.circe.{Decoder, Json}
import org.http4s.client.blaze.Http1Client
import org.http4s.client.oauth1.Consumer
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Headers, Request, Response, Status}

trait RequestF[T] extends HttpTypes with Logger {

  private[client] def jsonRequest[F[_] : Effect](request: Request[F], accessToken: Option[OAuthAccessToken] = None)
                                                (implicit consumer: Consumer,
                                                 decoder: Decoder[T]): Stream[F, HttpResponse[T]] = {

    fetchResponse(request, accessToken)(res => validateContentType(parseJson[F])(res))
  }

  private[client] def plainTextRequest[F[_] : Effect](request: Request[F], accessTokenRequest: Option[AccessTokenRequest] = None)
                                                     (plainTextToDomainPipe: PipeTransform[F, String, T])
                                                     (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    fetchResponse(request, accessTokenRequest)(res =>
      Stream.eval(res.as[String])
        .attempt
        .through(errorPipe(res.status))
        .through(plainTextToDomainPipe)
    )
  }

  private def fetchResponse[F[_] : Effect](request: Request[F], accessToken: Option[OAuthAccessToken] = None)
                                          (f: Response[F] => Stream[F, ErrorOr[T]])
                                          (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer, accessToken)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      client <- Http1Client.stream[F]()
      req <- signed
      response <- client.streaming(req)(withLogger { res =>
        f(res).map(HttpResponse(res.headers, _))
      })
    } yield response
  }

  private def validateContentType[F[_] : Effect](f: Response[F] => StreamResponse[F, T])
                                                (response: Response[F]): StreamResponse[F, T] = {
    response.headers.get(CaseInsensitiveString("Content-Type")).map(_.value) match {
      case None | Some("application/json") => f(response)
      case Some("text/plain") if response.status != Status.Ok =>
        Stream.eval(response.as[String]).map(str => Left(ResponseError(new Exception(str), response.status)))
      case Some(contentType) => Stream.emit(ResponseError(
        new Exception(s"$contentType: unexpected Content-Type"), Status.UnsupportedMediaType)).map(Left(_))
    }
  }

  private def parseJson[F[_] : Effect](response: Response[F])
                                      (implicit decode: Decoder[T]): StreamResponse[F, T] = {
    val jsonStream: Stream[F, Json] = response
      .body
      .through(byteStreamParser)
      .through(jsonLogPipe)

    response.status match {
      case Status.Ok => jsonStream
        .through(decoder[F, T])
        .attempt
        .map(_.left.map(ResponseError(_)))
      case _ =>
        jsonStream.map(json =>
          Left(ResponseError(new Exception(json.spaces2), response.status)))
    }
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

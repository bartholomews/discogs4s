package client

import cats.effect.Effect
import entities.ResponseError
import fs2.{Pipe, Pure, Stream}
import io.circe.fs2.{byteStreamParser, decoder}
import io.circe.{Decoder, Json}
import org.http4s.client.blaze.Http1Client
import org.http4s.client.oauth1.Consumer
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Headers, Request, Response, Status}
import utils.{Logger, Types}

trait RequestF[T] extends Types with Logger {

  val emptyResponse: ResponseError = ResponseError(
      new Exception("Response was empty. Please check request logs."),
      Status.BadRequest
  )

  val emptyHttpResponse: HttpResponse[Nothing] = HttpResponse(
    Status.BadRequest,
    Headers.empty,
    Left(emptyResponse)
  )

  def jsonRequest[F[_] : Effect](request: Request[F])
                                (implicit consumer: Consumer,
                                 decoder: Decoder[T]): Stream[F, HttpResponse[T]] = {

    fetchResponse(request)(res => validateContentType(parseJson[F])(res))
  }

  def plainTextRequest[F[_] : Effect](request: Request[F])
                                     (plainTextToDomainPipe: PipeTransform[F, String, T])
                                     (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    fetchResponse(request)(res =>
      Stream.eval(res.as[String]).attempt.through(errorPipe).through(plainTextToDomainPipe)
    )
  }

  private def fetchResponse[F[_] : Effect](request: Request[F])
                                  (f: Response[F] => Stream[F, ErrorOr[T]])
                                  (implicit consumer: Consumer): Stream[F, HttpResponse[T]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      client   <- Http1Client.stream[F]()
      req      <- signed
      response <- client.streaming(req)(withLogger { res =>
        f(res).map(HttpResponse(res.status, res.headers, _))
      })
    } yield response
  }

  private def validateContentType[F[_] : Effect](f: Response[F] => StreamResponse[F, T])
                                        (response: Response[F]): StreamResponse[F, T] = {
    response.headers.get(CaseInsensitiveString("Content-Type")).map(_.value) match {
      case None | Some("application/json") => f(response)
      case Some(contentType) =>
        val str: Stream[F, ResponseError] = Stream.emit(ResponseError(
          new Exception(s"$contentType: unexpected Content-Type"), Status.UnsupportedMediaType)
        )
        str.map(error => Left(error))
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

  private def fetch[F[_] : Effect](request: Request[F])
                                  (f: Response[F] => StreamResponse[F, T])
                                  (implicit consumer: Consumer): StreamResponse[F, T] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      client   <- Http1Client.stream[F]()
      req      <- signed
      response <- client.streaming(req)(resp => f(resp))
    } yield response
  }

  private def errorPipe[F[_], U]: Pipe[F, Either[Throwable, U], ErrorOr[U]] =
    _.map(_.left.map(ResponseError(_)))

  import org.http4s.client.oauth1._

  private def sign[F[_] : Effect](consumer: Consumer, token: Option[Token] = None)
                                 (req: Request[F]): F[Request[F]] = {

    import java.nio.charset.StandardCharsets
    import java.util.Base64

    signRequest(
      req,
      consumer,
      callback = None,
      verifier = Some(Base64.getEncoder.encodeToString(s"${consumer.key}:${consumer.secret}"
        .getBytes(StandardCharsets.UTF_8))),
      token
    )
  }

}

case class HttpResponse[T](status: Status,
                           headers: Headers,
                           entity: Either[ResponseError, T])

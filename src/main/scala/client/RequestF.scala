package client

import cats.effect.{Effect, IO}
import entities.ResponseError
import fs2.{Pipe, Pure, Stream}
import io.circe.Decoder
import io.circe.fs2.{byteStreamParser, decoder}
import org.http4s.client.blaze.Http1Client
import org.http4s.client.oauth1.Consumer
import org.http4s.{Headers, Request, Response, Status}
import utils.Logger

trait RequestF[T] extends Logger {

  def plainTextRequest[F[_] : Effect](request: Request[F])
                                     (pipe: Pipe[F, Either[ResponseError, String], Either[ResponseError, T]])
                                     (implicit consumer: Consumer): Stream[F, Either[ResponseError, T]] = {

    fetch(request)(res =>
      withLogger(Stream.eval(res.as[String]).attempt.through(errorPipe)).through(pipe))
  }

  def fetchJson(request: Request[IO])(implicit consumer: Consumer, decode: Decoder[T]): IO[T] = {

    def parseJson[F[_] : Effect](response: Response[F]): Stream[F, Either[ResponseError, T]] = {
      val headers: Headers = response.headers

      val status = headers.find(_.name == "Content-Type").map(contentType => {
        if (contentType.value == "application/json") Status.Ok else Status.BadRequest
      }).getOrElse(response.status)

      val jsonStream = response.body.through(byteStreamParser).through(jsonBodyLogger)

      status match {
        case Status.Ok => jsonStream.through(decoder[F, T])
          .attempt
          .map(_.left.map(ResponseError(_)))
        case _ =>
          jsonStream.map(json => Left(ResponseError(new Exception(json.spaces2), status)))
      }
    }

    def streamJson[F[_] : Effect](request: Request[F]): Stream[F, Either[ResponseError, T]] = {
      fetch(request)(withLogger(res => parseJson(res)))
    }

    streamJson[IO](withLogger(request))
      .evalMap(IO.fromEither)
      .compile
      .toList
      .map(_.head) // FIXME exception head of empty list :(
  }

  private def fetch[F[_] : Effect](request: Request[F])
                                  (f: Response[F] => Stream[F, Either[ResponseError, T]])
                                  (implicit consumer: Consumer): Stream[F, Either[ResponseError, T]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      client <- Http1Client.stream[F]()
      req <- signed
      response <- client.streaming(req)(resp => f(resp))
    } yield response
  }

  private def errorPipe[F[_] : Effect, U]: Pipe[F, Either[Throwable, U], Either[ResponseError, U]] = stream =>
    stream.map(_.left.map(ResponseError(_)))

  import org.http4s.client.oauth1._

  private def sign[F[_] : Effect]
  (consumer: Consumer, token: Option[Token] = None)
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

package client

import cats.effect.{Effect, IO}
import entities.ResponseError
import fs2.{Pipe, Pure, Stream}
import io.circe.Decoder
import io.circe.fs2.{byteStreamParser, decoder}
import org.http4s.{Request, Response, Status}
import org.http4s.client.blaze.Http1Client
import org.http4s.client.oauth1.Consumer
import utils.Logger

trait RequestF[T] extends Logger {

  def plainTextRequest[F[_] : Effect](request: Request[F])
                              (pipe: Pipe[F, Either[Throwable, String], Either[Throwable, T]])
                              (implicit consumer: Consumer): Stream[F, Either[Throwable, T]] = {
     fetch(request)(res =>
       withLogger(Stream.eval(res.as[String]).attempt).through(pipe))
  }

  def fetch[F[_] : Effect](request: Request[F])
                          (f: Response[F] => Stream[F, Either[Throwable, T]])
                          (implicit consumer: Consumer): Stream[F, Either[Throwable, T]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      client <- Http1Client.stream[F]()
      req <- signed
      response <- client.streaming(req)(resp => f(resp))
    } yield response
  }

  def process(request: Request[IO])(implicit consumer: Consumer, decode: Decoder[T]): IO[T] = {

    def parseJson[F[_] : Effect](response: Response[F]): Stream[F, Either[Throwable, T]] = {
      val status = response.status
      val headers = response.headers
      // TODO if response = plainText don't bother and return Left
      val jsonStream = response.body.through(byteStreamParser).through(jsonBodyLogger)
      status match {
        case Status.Ok => jsonStream.through(decoder[F, T])
          .attempt
          .map(_.left.map(ResponseError(_, Status.SeeOther)))
        case _ =>
          jsonStream.map(e => e.toString())
          jsonStream.map(_ => Left(ResponseError(new Exception("Oops"), status)))
      }
    }

    def fetchJson[F[_] : Effect](request: Request[F]): Stream[F, Either[Throwable, T]] = {
      fetch(request)(withLogger(res => parseJson(res)))
    }

    fetchJson[IO](withLogger(request))
      .evalMap(IO.fromEither)
      .compile
      .toList
      .map(_.head) // FIXME exception head of empty list :(
  }

  import org.http4s.client.oauth1._

  private def sign[F[_] : Effect]
  (consumer: Consumer, token: Option[Token] = None)
  (req: Request[F]): F[Request[F]] = {

    import java.util.Base64
    import java.nio.charset.StandardCharsets

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

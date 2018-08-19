package client

import cats.effect.IO
import client.api.AccessTokenRequest
import io.circe.Decoder
import org.http4s.Request
import org.http4s.client.oauth1.{Consumer, Token}

trait IOClient[T] extends RequestF[T] {

  private implicit class IOStreamUtils(stream: fs2.Stream[IO, HttpResponse[T]]) {
    implicit def io: IO[HttpResponse[T]] = stream
      .compile
      .last
      .flatMap(_.toRight(emptyResponse).fold(
        empty => IO.raiseError(empty),
        value => IO.pure(value)
      ))
  }

  def fetchPlainText(request: Request[IO], accessTokenRequest: Option[AccessTokenRequest] = None)
                    (implicit pipeTransform: PipeTransform[IO, String, T],
                     consumer: Consumer): IO[HttpResponse[T]] = {

    plainTextRequest[IO](withLogger(request), accessTokenRequest)(pipeTransform).io
  }

  def fetchJson(request: Request[IO], token: Option[Token] = None)
               (implicit consumer: Consumer, decode: Decoder[T]): IO[T] = {

    jsonRequest(withLogger(request), token)
      .evalMap(res => IO.fromEither(res.entity))
      .compile
      .last
      .flatMap(_.toRight(emptyResponse).fold(
        empty => IO.raiseError(empty),
        value => IO.pure(value)
      ))
  }
}
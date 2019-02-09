package client.http

import api.{AccessTokenRequest, OAuthAccessToken}
import cats.effect.IO
import entities.ResponseError
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer
import org.http4s.{Headers, Request}

trait IOClient[T] extends RequestF[T] {

  private implicit class IOStreamUtils(stream: fs2.Stream[IO, HttpResponse[T]]) {
    implicit def io: IO[HttpResponse[T]] = stream
      .compile
      .last
      .flatMap(_.toRight(ResponseError.empty).fold(
        empty => IO.pure(HttpResponse(Headers(), Left(empty))),
        value => IO.pure(value)
      ))
  }

  private[client] def fetchPlainText(client: Client[IO])(request: Request[IO], accessTokenRequest: Option[AccessTokenRequest] = None)
                                    (implicit consumer: Consumer, pipeTransform: PipeTransform[IO, String, T]): IO[HttpResponse[T]] = {

    plainTextRequest[IO](client)(request, accessTokenRequest)(pipeTransform)
      .io
  }

  private[client] def fetchJson(client: Client[IO])(request: Request[IO], token: Option[OAuthAccessToken] = None)
                               (implicit consumer: Consumer, decode: Decoder[T]): IO[HttpResponse[T]] = {

    jsonRequest(client)(request, token)
      //.evalMap(res => IO.fromEither(res.entity))
      .io
  }
}

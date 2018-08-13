package client

import cats.effect.IO
import io.circe.Decoder
import org.http4s.Request
import org.http4s.client.oauth1.Consumer

trait IOClient[T] extends RequestF[T] {

  def fetchJson(request: Request[IO])
               (implicit consumer: Consumer, decode: Decoder[T]): IO[T] = {

    jsonRequest(withLogger(request))
      .evalMap(res => IO.fromEither(res.entity))
      .compile
      .last
      .flatMap(_.toRight(emptyResponse).fold(
        empty => IO.raiseError(empty),
        value => IO.pure(value)
      ))
  }
}
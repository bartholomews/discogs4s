package client.effect4s

import cats.effect.IO
import client.effect4s.entities.{HttpResponse, ResponseError}
import org.http4s.Headers

trait IOClient extends HttpEffectsClient[IO] {

  override def run[A]: fs2.Stream[IO, HttpResponse[A]] => IO[HttpResponse[A]] = stream =>
    stream
      .compile
      .last
      .flatMap(_.toRight(ResponseError.empty).fold(
        empty => IO.pure(HttpResponse(Headers(), Left(empty))),
        value => IO.pure(value)
      ))
}

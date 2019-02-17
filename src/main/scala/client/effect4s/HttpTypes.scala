package client.effect4s

import cats.effect.IO
import client.effect4s.entities.{HttpResponse, ResponseError}
import fs2.Pipe

trait HttpTypes {
  type HttpPipe[F[_], A, B] = Pipe[F, ErrorOr[A], ErrorOr[B]]
  type IOResponse[T] = IO[HttpResponse[T]]
  type ErrorOr[T] = Either[ResponseError, T]
}

package client.utils

import cats.effect.IO
import entities.ResponseError
import client.http.HttpResponse
import fs2.{Pipe, Stream}

trait HttpTypes {
  type StreamResponse[F[_], T] = Stream[F, ErrorOr[T]]
  type PipeTransform[F[_], A, B] = Pipe[F, ErrorOr[A], ErrorOr[B]]
  type IOResponse[T] = IO[HttpResponse[T]]
  type ErrorOr[T] = Either[ResponseError, T]
}

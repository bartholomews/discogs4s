package utils

import entities.ResponseError
import fs2.{Pipe, Stream}

trait Types {
  type StreamResponse[F[_], T] = Stream[F, ErrorOr[T]]
  type PipeTransform[F[_], A, B] = Pipe[F, ErrorOr[A], ErrorOr[B]]
  type ErrorOr[T] = Either[ResponseError, T]
}

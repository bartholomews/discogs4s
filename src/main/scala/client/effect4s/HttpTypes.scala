package client.effect4s

import cats.effect.IO
import client.effect4s.entities.{HttpResponse, ResponseError}
import fs2.{Pipe, Stream}

trait HttpTypes {
  type StreamResponse[F[_], T] = Stream[F, ErrorOr[T]]
  type PipeTransform[F[_], A, B] = Pipe[F, ErrorOr[A], ErrorOr[B]]
  type EffectResponse[F[_], T] = F[HttpResponse[T]]
  type IOResponse[T] = IO[HttpResponse[T]]
  type ErrorOr[T] = Either[ResponseError, T]
}

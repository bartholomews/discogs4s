package utils

import cats.effect.IO
import client.HttpResponse
import entities.ResponseError
import fs2.{Pipe, Stream}
import org.http4s.Status

trait HttpResponseUtils {
  type StreamResponse[F[_], T] = Stream[F, ErrorOr[T]]
  type PipeTransform[F[_], A, B] = Pipe[F, ErrorOr[A], ErrorOr[B]]
  type IOResponse[T] = IO[HttpResponse[T]]
  type ErrorOr[T] = Either[ResponseError, T]

  val emptyResponse: ResponseError = ResponseError(
    new Exception("Response was empty. Please check request logs."),
    Status.BadRequest
  )
}

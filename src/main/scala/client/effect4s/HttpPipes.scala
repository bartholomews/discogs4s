package client.effect4s

import cats.effect.Effect
import client.effect4s.entities.ResponseError
import fs2.{Pipe, Stream}
import org.http4s.{Response, Status}
import cats.implicits._
import io.circe.Decoder
import io.circe.fs2.{byteStreamParser, decoder}
import org.http4s.headers.`Content-Type`

trait HttpPipes extends HttpTypes with Logger {

  private[effect4s] def doNothing[F[_] : Effect, A]: Pipe[F, A, A] = _.map(identity)

  /**
    * Attempt to decode an Http Response with the provided decoder
    * @param decode a decoder for type `A`
    * @tparam F the `Effect`
    * @tparam A the type of expected response entity
    * @return a Pipe transformed in an `Either[ResponseError, A]`
    */
  private[effect4s] def decodeJsonResponse[F[_] : Effect, A]
  (implicit decode: Decoder[A]): Pipe[F, Response[F], ErrorOr[A]] =
    _.flatMap(
      _.body
        .through(byteStreamParser)
        .through(decoder[F, A])
        .attempt
        .through(leftMapToResponseError(Status.UnprocessableEntity))
    )

  /**
    * Attempt to decode a PlainText Http Response with the provided decoder
    * @param decoder a Pipe decoder from String to type `A`
    * @tparam F the `Effect`
    * @tparam A the type of expected response entity
    * @return a Pipe transformed in an `Either[ResponseError, A]`
    */
  private[effect4s] def decodeTextPlainResponse[F[_] : Effect, A]
  (implicit decoder: HttpPipe[F, String, A]): Pipe[F, Response[F], ErrorOr[A]] =
    _.flatMap(res => {
      Stream.eval(res.as[String])
        .attempt
        .through(leftMapToResponseError[F, String](Status.UnprocessableEntity))
        .through(decoder)
    })

  /**
    * Map the left side of an `Either[Throwable, A]` into a `ResponseError`
    *
    * @param status the Status Code of the `ResponseError`
    * @tparam F the `Effect`
    * @tparam A the type of expected response entity
    * @return an `Either[ResponseError, A]`
    */
  private[effect4s] def leftMapToResponseError[F[_] : Effect, A]
  (status: Status): Pipe[F, Either[Throwable, A], ErrorOr[A]] =
    _.through(errorLogPipe)
      .map(
        _.fold(
          err => Left(ResponseError(err, status)),
          res => Right(res)
        )
      )

  /**
    * Fold both sides of an `Either[Throwable, A]` into an `Either.left[ResponseError]`
    *
    * @param status the Status Code of the `ResponseError`
    * @param f      function to map the `A` into the error message
    * @tparam F the `Effect`
    * @tparam A the type of expected response entity, which will be folded to the left
    * @return a Pipe transformed in an `Either.left[ResponseError, Nothing]`
    */
  private[effect4s] def foldToResponseError[F[_] : Effect, A]
  (status: Status, f: A => String = (res: A) => res.toString): Pipe[F, Either[Throwable, A], ErrorOr[Nothing]] =
    _.through(errorLogPipe)
      .map(e => e.fold(
        err => ResponseError(err, status).asLeft,
        res => ResponseError(new Exception(f(res)), status).asLeft
      ))

  /**
    * Decode an Http Response into an `Either.left[ResponseError, Nothing]`.
    * @tparam F the `Effect`
    * @return a Pipe transformed in an `Either.left[ResponseError, Nothing]`
    */
  private[effect4s] def errorHandler[F[_] : Effect]: Pipe[F, Response[F], ErrorOr[Nothing]] =
    _.flatMap(
      response => {
        response.headers.get(`Content-Type`).map(_.value) match {
          case Some("application/json") =>
            response
              .body
              .through(byteStreamParser)
              .attempt
              // FIXME: could try to parse a { "message": "[value]" } instead of _.spaces2
              .through(foldToResponseError(response.status, _.spaces2))

          case Some("text/plain") => Stream.eval(response.as[String])
            .attempt
            .through(foldToResponseError(response.status))

          case Some(unexpectedContentType) =>
            Stream.emit(new Exception(s"$unexpectedContentType: unexpected `Content-Type`").asLeft)
              .through(foldToResponseError(Status.UnsupportedMediaType))

          case None =>
            Stream.emit(new Exception("`Content-Type` not provided").asLeft)
              .through(foldToResponseError(Status.UnsupportedMediaType))
        }
      }
    )

}

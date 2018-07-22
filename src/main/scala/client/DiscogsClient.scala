package client

import cats.effect.{Effect, IO}
import entities.{DiscogsEntity, ResponseError}
import fs2.Stream
import io.circe.Decoder
import io.circe.fs2.{
  byteStreamParser,
  decoder
}
import org.http4s.client.blaze.Http1Client
import org.http4s.{Method, Request, Status, Uri}
import utils.Logger

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

// https://http4s.org/v0.19/streaming/
case class DiscogsClient() extends Logger {

  //TODO User-Agent
  // $APP_NAME/$APP_VERSION +$APP_URL
  // e.g. Bidwish/1.0 (+https://github.com/bartholomews/bidwish)

  /*
  TODO extract headers
  We attach the following headers to responses to help you track your rate limit use:
  X-Discogs-Ratelimit: The total number of requests you can make in a one minute window.
  X-Discogs-Ratelimit-Used : The number of requests you’ve made in your existing rate limit window.
  X-Discogs-Ratelimit-Remaining: The number of remaining requests you are able to make in the existing rate limit window.
  Your application should take our global limit into account and throttle its requests locally.
   */

  def fetch[F[_] : Effect, T](request: Request[F])
                             (implicit decode: Decoder[T]): Stream[F, Either[Throwable, T]] = {

    Http1Client.stream[F]().flatMap(_.streaming(request)(response => {
      val status = response.status
      println(s"STATUS: $status")
      val headers = response.headers
      println("HEADERS:")
      headers.foreach(println(_))
      val jsonStream = response.body.through(byteStreamParser).through(jsonBodyLogger)
      status match {
        case Status.Ok => jsonStream.through(decoder[F, T]).attempt map {
          _.left.map(ResponseError(_, Status.SeeOther))
        }
        case _ => jsonStream.map(_ => Left(ResponseError(new Exception("Oops"), status)))
      }
    }))
  }

  //    val statusStream: Stream[F, Status] = stream.evalMap(c => c.status(request))
  //
  //    val jsonStream = stream
  //      .flatMap(_.streaming(request)(resp => resp.body))
  //      .through(byteStreamParser)
  //      .through(jsonBodyLogger)
  //
  //    statusStream flatMap(status => {
  /*
  200 OK - The request was successful, and the requested data is provided in the response body.
  201 Continue - You’ve sent a POST request to a list of resources to create a new one. The ID of the newly-created resource will be provided in the body of the response.
  204 No Content - The request was successful, and the server has no additional information to convey, so the response body is empty.
  401 Unauthorized - You’re attempting to access a resource that first requires authentication. See Authenticating with OAuth.
  403 Forbidden - You’re not allowed to access this resource. Even if you authenticated, or already have, you simply don’t have permission. Trying to modify another user’s profile, for example, will produce this error.
  404 Not Found - The resource you requested doesn’t exist.
  405 Method Not Allowed - You’re trying to use an HTTP verb that isn’t supported by the resource. Trying to PUT to /artists/1, for example, will fail because Artists are read-only.
  422 Unprocessable Entity - Your request was well-formed, but there’s something semantically wrong with the body of the request. This can be due to malformed JSON, a parameter that’s missing or the wrong type, or trying to perform an action that doesn’t make any sense. Check the response body for specific information about what went wrong.
  500 Internal Server Error - Something went wrong on our end while attempting to process your request. The response body’s message field will contain an error code that you can send to Discogs Support (which will help us track down your specific issue).
   */
  //      println("STATUS=========")
  //      println(status)
  //    })
  //  }

  //  def requestIO[T](maybeUri: Try[Uri], method: Method)
  //                  (implicit decode: Decoder[T]): IO[T] = {
  //    maybeUri match {
  //      case Success(uri) =>
  //        fetch[IO, T](Log(Request[IO](method, uri)))
  //          .evalMap(IO.fromEither)
  //          .compile
  //          .toList
  //          .map(_.head) // FIXME exception head of empty list :(
  //      case Failure(throwable) => LogError(IO.raiseError(ResponseError(throwable, Status.BadRequest)))
  //    }
  //  }

  sealed trait RequestIO[T] {

    def process(uri: Uri)(request: Uri => Request[IO])
               (implicit decoder: Decoder[T]): IO[T] = {

      fetch[IO, T](Log(request(uri)))
        .evalMap(IO.fromEither)
        .compile
        .toList
        .map(_.head) // FIXME exception head of empty list :(
    }
  }

  case class GET[T <: DiscogsEntity](private val api: Api[T])
                                    (implicit decoder: Decoder[T]) extends RequestIO[T] {

    def io: IO[T] = process(api.uri)(uri => Request[IO](Method.GET, uri))

    def ioEither: IO[Either[Throwable, T]] = io.attempt

    def ioTry: IO[Try[T]] = ioEither.map(_.fold(
      throwable => Failure(throwable),
      response => Success(response)
    ))
  }

}

package client.effect4s

import cats.effect.Effect
import client.effect4s.entities.{HttpResponse, OAuthAccessToken}
import fs2.{Pipe, Pure, Stream}
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer
import org.http4s.{Request, Response, Status}

private[effect4s] trait RequestF extends HttpPipes with HttpTypes with OAuthSignature with Logger {

  def jsonRequest[F[_] : Effect, A](client: Client[F])
                                   (request: Request[F],
                                    accessToken: Option[OAuthAccessToken] = None)
                                   (implicit
                                    consumer: Consumer,
                                    decoder: Decoder[A]): Stream[F, HttpResponse[A]] = {

    processHttpRequest(client)(request, accessToken, decodeJsonResponse, doNothing)
  }

  def plainTextRequest[F[_] : Effect, A](client: Client[F])
                                        (request: Request[F],
                                         accessToken: Option[OAuthAccessToken] = None)
                                        (implicit consumer: Consumer,
                                         decoder: HttpPipe[F, String, A]): Stream[F, HttpResponse[A]] = {

    processHttpRequest(client)(request, accessToken, decodeTextPlainResponse, decoder)
  }

  private def processHttpRequest[F[_] : Effect, A](client: Client[F])
                                                  (request: Request[F],
                                                   accessToken: Option[OAuthAccessToken] = None,
                                                   decodeRight: Pipe[F, Response[F], ErrorOr[A]],
                                                   decodeLeft: Pipe[F, ErrorOr[Nothing], ErrorOr[A]])
                                                  (implicit consumer: Consumer): Stream[F, HttpResponse[A]] = {

    val signed: Stream[F, Request[F]] = Stream.eval(sign(consumer, accessToken)(request))
    val pure: Stream[Pure, Request[F]] = Stream(request)

    for {
      request <- signed.through(requestHeadersLogPipe)
      response <- client.stream(request).through(responseHeadersLogPipe)
      httpRes <- (response.status match {
        case Status.Ok =>
          Stream.emit(response)
            .through(decodeRight)
            .through(responseLogPipe)

        case _ =>
          Stream.emit(response)
            .through(errorHandler)
            .through(decodeLeft)
            .through(responseLogPipe)

      }).map(HttpResponse(response.headers, _))

    } yield httpRes
  }
}
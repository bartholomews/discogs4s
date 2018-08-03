package client

import cats.effect.{Effect, IO}
import client.api.{AuthorizeUrl, DiscogsApi}
import entities.DiscogsEntity
import fs2.Pipe
import io.circe.Decoder
import org.http4s.client.oauth1.Consumer
import org.http4s.{Header, Headers, Method, Request, Uri}
import utils.{Config, ConsumerConfig, Logger}

import scala.util.{Failure, Success, Try}

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication
case class DiscogsClient(consumerClient: Option[ConsumerConfig] = None) extends Logger {

  private val consumerConfig = consumerClient.getOrElse(Config.CONSUMER_CONFIG) // todo handle error
  private implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private def get[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withMethod(Method.GET)
    .withUri(uri)
    .withHeaders(USER_AGENT)

  case class GET[T <: DiscogsEntity](private val api: DiscogsApi[T])
                                    (implicit decoder: Decoder[T]) extends RequestF[T] {

    def io: IO[T] = process(get(api.uri))

    def ioEither: IO[Either[Throwable, T]] = io.attempt

    def ioTry: IO[Try[T]] = ioEither.map(_.fold(
      throwable => Failure(throwable),
      response => Success(response)
    ))
  }

  /*
  TODO extract status
  200 OK - The request was successful, and the requested data is provided in the response body.
  201 Continue - You’ve sent a POST request to a list of resources to create a new one. The ID of the newly-created resource will be provided in the body of the response.
  204 No Content - The request was successful, and the server has no additional information to convey, so the response body is empty.
  401 Unauthorized - You’re attempting to access a resource that first requires authentication. See Authenticating with OAuth.
  403 Forbidden - You’re not allowed to access this resource. Even if you authenticated, or already have, you simply don’t have permission. Trying to modify another user’s profile, for example, will produce this error.
  404 Not Found - The resource you requested doesn’t exist.
  405 Method Not Allowed - You’re trying to use an HTTP verb that isn’t supported by the resource. Trying to PUT to /artists/1, for example, will fail because Artists are read-only.
  422 Unprocessable Entity - Your request was well-formed, but there’s something semantically wrong with the body of the request. This can be due to malformed JSON, a parameter that’s missing or the wrong type, or trying to perform an action that doesn’t make any sense. Check the response body for specific information about what went wrong.500 Internal Server Error - Something went wrong on our end while attempting to process your request. The response body’s message field will contain an error code that you can send to Discogs Support (which will help us track down your specific issue).
  */
  /*
  TODO extract headers
  We attach the following headers to responses to help you track your rate limit use:
  X-Discogs-Ratelimit: The total number of requests you can make in a one minute window.
  X-Discogs-Ratelimit-Used : The number of requests you’ve made in your existing rate limit window.
  X-Discogs-Ratelimit-Remaining: The number of remaining requests you are able to make in the existing rate limit window.
  Your application should take our global limit into account and throttle its requests locally.
  */

  case object OAUTH extends RequestF[Uri] {

    def getAuthoriseUrl: IO[Either[Throwable, Uri]] = {
      val oAuthQueryResponse = ("oauth_token=(.*)" +
        "&oauth_token_secret=(.*)" +
        "&oauth_callback_confirmed=(.*)").r

      val invalidSignature = "Invalid signature. (.*)".r
      val emptyResponse = "Response was empty, please check request uri"

      val pipe: Pipe[IO, Either[Throwable, String], Either[Throwable, Uri]] = stream => {
        stream
          .last
          .map(opt => opt.toLeft(emptyResponse).joinLeft)
          .map {
            case Right(oAuthQueryResponse(token, _, _)) =>
              Right(AuthorizeUrl.response(token))
            // TODO case Left after plainText handles properly response on Status
            case Right(invalidSignature(_)) =>
              Left(new Exception("Invalid signature. Please double check consumer secret key."))
            // TODO case Left after plainText handles properly response on Status
            case Right(response) =>
              Left(new Exception(if (response.isEmpty) emptyResponse else response))
          }
      }

      plainTextRequest[IO](withLogger(get(AuthorizeUrl.uri)))(pipe)
        .compile
        .last
        .map(opt => opt.toRight(
          new Exception("Response was empty. Please check request logs."
        )).joinRight)
    }
  }

}

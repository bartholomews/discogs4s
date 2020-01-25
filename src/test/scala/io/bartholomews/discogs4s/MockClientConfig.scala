package io.bartholomews.discogs4s

import cats.effect.{ContextShift, IO, Resource}
import fsclient.config.{FsClientConfig, UserAgent}
import fsclient.entities.AuthEnabled
import fsclient.entities.AuthVersion.V1
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

trait MockClientConfig {

  import scala.concurrent.ExecutionContext

  // https://http4s.org/v0.20/client/
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  def validClient: DiscogsClient = clientWith()

  def clientWith(key: String = validConsumerKey,
                 secret: String = validConsumerSecret,
                 appName: String = "someApp",
                 appVersion: Option[String] = Some("1.0"),
                 appUrl: Option[String] = Some("app.git")): DiscogsClient =
    new DiscogsClient(
      FsClientConfig(UserAgent(appName, appVersion, appUrl), AuthEnabled(V1.BasicSignature(Consumer(key, secret))))
    )

  val validConsumerKey = "VALID_CONSUMER_KEY"
  val validConsumerSecret = "VALID_CONSUMER_SECRET"
  val consumerWithInvalidSignature = "INVALID_SIGNATURE_CONSUMER"
  val consumerGettingUnexpectedResponse = "UNEXPECTED_RESPONSE_CONSUMER"

  val validToken = "TOKEN"
  val validSecret = "SECRET"
  val validVerifier = "VERIFIER"
  val emptyResponseMock = "EMPTY_RESPONSE"

  val unexpectedResponse = "Something went wrong."

  val emptyResponseEndpoint = "empty-response"
  val notFoundResponseEndpoint = "not-found"
  val unsupportedMediaTypeBadRequestEndpoint = "unsupported-media-type-400"
  val unsupportedMediaTypeOkEndpoint = "unsupported-media-type-200"
  val noHeadersBadRequest = "no-content-type-bad-request"

  val validConsumer = Consumer(validConsumerKey, validConsumerSecret)

//  def getOAuthClient(discogsSimpleClient: DiscogsSimpleClient, verifier: String): IO[Either[ResponseError, DiscogsOAuthClient]] = {
//    (for {
//      requestTokenResponse <- EitherT(discogsSimpleClient.requestToken.map(_.e1ntity))
//      req: OAuthEndpoint[AccessToken] = AccessTokenApi(RequestToken(requestTokenResponse.token, Some(validVerifier)))
//      res <- EitherT(discogsSimpleClient.client.toOAuthClient(req))
//    } yield new DiscogsOAuthClient(res)).value
//  }
}
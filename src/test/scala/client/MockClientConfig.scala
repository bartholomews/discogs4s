package client

import cats.effect.{ContextShift, IO}
import client.utils.Config.ConsumerConfig
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.client.oauth1.Consumer

trait MockClientConfig {

  import scala.concurrent.ExecutionContext
  import java.util.concurrent._

  // https://http4s.org/v0.20/client/
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(global)
  private val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  val blockingClientIO: Client[IO] = JavaNetClientBuilder[IO](blockingEC).create

  // FIXME should be OAuthClient
  def validOAuthClient: DiscogsSimpleClient = clientWith()

  def clientWith(key: String = validConsumerKey,
                 secret: String = validConsumerSecret,
                 appName: String = "someApp",
                 appVersion: Option[String] = Some("1.0"),
                 appUrl: Option[String] = Some("app.git")): DiscogsSimpleClient =

    new DiscogsSimpleClient(ConsumerConfig(appName, appVersion, appUrl, key, secret))(global)

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

  val validConsumer = Consumer(validConsumerKey, validConsumerSecret)
}
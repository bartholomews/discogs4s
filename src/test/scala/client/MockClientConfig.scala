package client

import org.http4s.client.oauth1.Consumer
import utils.ConsumerConfig

trait MockClientConfig {

  def validOAuthClient: DiscogsClient = clientWith()

  def clientWith(key: String = validConsumerKey,
                 secret: String = validConsumerSecret,
                 appName: String = "someApp",
                 appVersion: Option[String] = Some("1.0"),
                 appUrl: Option[String] = Some("app.git")): DiscogsClient =

    DiscogsClient(Some(ConsumerConfig(appName, appVersion, appUrl, key, secret)))

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
package client

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
}
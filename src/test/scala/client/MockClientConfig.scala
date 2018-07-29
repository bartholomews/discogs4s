package client

import utils.ConsumerConfig

trait MockClientConfig {

  def validOAuthClient: DiscogsClient = clientWith()

  def clientWith(key: String = validConsumerKey, secret: String = validConsumerSecret) =
    DiscogsClient(Some(mockConsumerConfig(key, secret)))

  def mockConsumerConfig(key: String = validConsumerKey, secret: String = validConsumerSecret) =
    ConsumerConfig("someApp", Some("1.0"), Some("app.git"), key, secret)

  val validConsumerKey = "VALID_CONSUMER_KEY"
  val validConsumerSecret = "VALID_CONSUMER_SECRET"
}



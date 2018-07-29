package utils

import com.typesafe.config.ConfigFactory

object Config {
  private val factory = ConfigFactory.load()
  val SCHEME: String = factory.getString("discogs.scheme")
  val DISCOGS_API: String = factory.getString("discogs.api")
  val DISCOGS_DOMAIN: String = factory.getString("discogs.domain")
  val CONSUMER_CONFIG = ConsumerConfig(
    factory.getString("consumer.app_name"),
    factory.getString("consumer.app_version"),
    factory.getString("consumer.app_url"),
    factory.getString("consumer.key"),
    factory.getString("consumer.secret"),
  )
}

case class ConsumerConfig(appName: String,
                          appVersion: String,
                          appUrl: String,
                          key: String,
                          secret: String)
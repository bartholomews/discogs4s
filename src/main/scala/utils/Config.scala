package utils

import com.typesafe.config.ConfigFactory

object Config {
  private val factory = ConfigFactory.load()
  val SCHEME: String = factory.getString("discogs.scheme")
  val DISCOGS_API: String = factory.getString("discogs.api")
}

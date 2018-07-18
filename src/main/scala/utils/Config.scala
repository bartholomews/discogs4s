package utils

import com.typesafe.config.ConfigFactory

object Config {
  val DISCOGS_API: String = ConfigFactory.load().getString("discogs.api")
}

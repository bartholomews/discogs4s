package client.utils

import com.typesafe.config.ConfigFactory

import scala.util.Try

object Config {
  private val factory = ConfigFactory.load()

  private val reference = factory.getConfig("discogs")
  val SCHEME: String = reference.getString("scheme")
  val DISCOGS_API: String = reference.getString("api")
  val DISCOGS_DOMAIN: String = reference.getString("domain")

  lazy val CONSUMER_CONFIG = ConsumerConfig(
    factory.getString("consumer.app_name"),
    Try(factory.getString("consumer.app_version")).toOption,
    Try(factory.getString("consumer.app_url")).toOption,
    factory.getString("consumer.key"),
    factory.getString("consumer.secret"),
  )
}

case class ConsumerConfig(appName: String,
                          appVersion: Option[String],
                          appUrl: Option[String],
                          key: String,
                          secret: String) {

  private val version = appVersion.map(version => s"/$version").getOrElse("")
  private val url = appUrl.map(url => s" (+$url)").getOrElse("")
  // "name/version +(url)"
  def userAgent: String = s"$appName$version$url"
}
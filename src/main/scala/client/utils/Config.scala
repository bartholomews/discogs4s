package client.utils

import org.http4s.Uri

import scala.util.Try

object Config {

  import pureconfig.generic.auto._
  import cats.implicits._

  private[utils] case class Discogs(api: String, domain: String, scheme: String)
  private[utils] case class DiscogsConfig(discogs: Discogs)
  private[utils] case class DiscogsReference(apiUri: Uri, baseUri: Uri)

  lazy val discogs: DiscogsReference = {

    val either: Either[Throwable, DiscogsReference] = for {
      conf <- Try(pureconfig.loadConfigOrThrow[DiscogsConfig]).toEither
      apiUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.api}")
      baseUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.domain}")
    } yield DiscogsReference(apiUri, baseUri)

    either.valueOr(throw _)
  }

  lazy val consumer: ConsumerConfig = pureconfig.loadConfigOrThrow[ConsumerConfig]

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
}

//object Config {
//  private val factory = ConfigFactory.load()
//
//  private val reference = factory.getConfig("discogs")
//  val SCHEME: String = reference.getString("scheme")
//  val DISCOGS_API: String = reference.getString("api")
//  val DISCOGS_DOMAIN: String = reference.getString("domain")
//
//  lazy val CONSUMER_CONFIG = ConsumerConfig(
//    factory.getString("consumer.app_name"),
//    Try(factory.getString("consumer.app_version")).toOption,
//    Try(factory.getString("consumer.app_url")).toOption,
//    factory.getString("consumer.key"),
//    factory.getString("consumer.secret"),
//  )
//}
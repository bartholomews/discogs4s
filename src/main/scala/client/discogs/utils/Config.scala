package client.discogs.utils

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

  lazy val consumer: DiscogsConsumer = pureconfig.loadConfigOrThrow[DiscogsConsumerConfig].discogsConsumer

  private[utils] case class DiscogsConsumerConfig(discogsConsumer: DiscogsConsumer)

  case class DiscogsConsumer(appName: String,
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
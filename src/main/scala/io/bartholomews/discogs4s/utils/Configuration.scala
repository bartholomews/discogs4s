package io.bartholomews.discogs4s.utils

import org.http4s.Uri
import pureconfig.ConfigSource

import scala.util.Try

private[discogs4s] object Configuration {

  import cats.implicits._
  import pureconfig.generic.auto._

  private val discogsConfig = ConfigSource.default.at("discogs")

  val discogs: DiscogsReference = (for {
    config <- Try(discogsConfig.loadOrThrow[DiscogsConfig]).toEither
    apiUri <- Uri.fromString(s"${config.scheme}://${config.api}")
    baseUri <- Uri.fromString(s"${config.scheme}://${config.domain}")
  } yield DiscogsReference(apiUri, baseUri)).valueOr(throw _)

  private case class DiscogsConfig(api: String, domain: String, scheme: String)
  case class DiscogsReference(apiUri: Uri, baseUri: Uri)
}

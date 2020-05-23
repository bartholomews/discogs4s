package io.bartholomews.discogs4s.utils

import org.http4s.Uri
import pureconfig.ConfigSource

import scala.util.Try

private[discogs4s] object Configuration {

  import cats.implicits._
  import pureconfig.generic.auto._

  val discogs: DiscogsReference = (for {
    conf <- Try(ConfigSource.default.loadOrThrow[Config]).toEither
    apiUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.api}")
    baseUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.domain}")
  } yield DiscogsReference(apiUri, baseUri)).valueOr(throw _)

  case class Config(discogs: Discogs)
  case class Discogs(api: String, domain: String, scheme: String)
  case class DiscogsReference(apiUri: Uri, baseUri: Uri)
}

package client.discogs.utils

import org.http4s.Uri

import scala.util.Try

object Config {

  import pureconfig.generic.auto._
  import cats.implicits._

  private[client] val discogs: DiscogsReference = (for {
    conf <- Try(pureconfig.loadConfigOrThrow[DiscogsConfig]).toEither
    apiUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.api}")
    baseUri <- Uri.fromString(s"${conf.discogs.scheme}://${conf.discogs.domain}")
  } yield DiscogsReference(apiUri, baseUri)).valueOr(throw _)

  private[discogs] case class Discogs(api: String, domain: String, scheme: String)

  private[discogs] case class DiscogsConfig(discogs: Discogs)

  private[discogs] case class DiscogsReference(apiUri: Uri, baseUri: Uri)

}
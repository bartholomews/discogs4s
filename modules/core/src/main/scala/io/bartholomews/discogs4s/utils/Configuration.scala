package io.bartholomews.discogs4s.utils

import pureconfig.ConfigSource
import sttp.model.Uri

private[discogs4s] object Configuration {

  import io.bartholomews.fsclient.core.config.sttpUriReader
  import pureconfig.generic.auto._

  val discogs: DiscogsReference =
    ConfigSource.default.at("discogs").loadOrThrow[DiscogsReference]

  case class DiscogsReference(apiUri: Uri, baseUri: Uri)
}

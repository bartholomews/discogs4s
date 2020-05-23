package io.bartholomews.discogs4s.endpoints

import io.bartholomews.discogs4s.utils.Configuration
import org.http4s.Uri

object DiscogsEndpoint {
  final val baseUri: Uri = Configuration.discogs.baseUri
  final val apiUri: Uri = Configuration.discogs.apiUri
}

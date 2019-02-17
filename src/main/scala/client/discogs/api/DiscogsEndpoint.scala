package client.discogs.api

import org.http4s.Uri
import client.discogs.utils.Config

trait DiscogsEndpoint[T] {
  val uri: Uri
  private[api] val baseUri = Config.discogs.baseUri
  private[api] val apiUri = Config.discogs.apiUri
}
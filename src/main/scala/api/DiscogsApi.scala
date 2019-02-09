package api

import org.http4s.Uri
import client.utils.Config

trait DiscogsApi[T] {
  val uri: Uri
  private[api] val baseUri = Config.discogs.baseUri
  private[api] val apiUri = Config.discogs.apiUri
}
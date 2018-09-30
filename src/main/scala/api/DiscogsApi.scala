package api

import org.http4s.Uri
import client.utils.Config

trait DiscogsApi[T] {
  val uri: Uri
  private[api] val baseUrl =
    Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}")
}
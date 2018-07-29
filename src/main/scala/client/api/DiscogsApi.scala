package client.api

import org.http4s.Uri
import utils.Config

trait DiscogsApi[T] {
  val uri: Uri
  private[client] val baseUrl =
    Uri.unsafeFromString(s"${Config.SCHEME}://${Config.DISCOGS_API}")
}
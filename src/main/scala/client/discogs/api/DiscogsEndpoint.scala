package client.discogs.api

import org.http4s.{Method, Uri}
import client.discogs.utils.Config
import org.http4s.Method.{DefaultMethodWithBody, SafeMethodWithBody}

trait DiscogsEndpoint[T] {
  val uri: Uri
  val method: Method
  private[api] val baseUri = Config.discogs.baseUri
  private[api] val apiUri = Config.discogs.apiUri
}

sealed trait HttpMethod {
  def method: Method
}

object HttpMethod {
  trait GET extends HttpMethod {
    override val method: SafeMethodWithBody = Method.GET
  }

  trait POST extends HttpMethod {
    override val method: DefaultMethodWithBody = Method.POST
  }
}




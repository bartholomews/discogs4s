package io.bartholomews.discogs4s.entities

import fsclient.codecs.FsJsonResponsePipe
import io.bartholomews.discogs4s.api.AuthorizeUrl
import io.bartholomews.discogs4s.utils.Configuration
import org.http4s.Uri
import org.http4s.client.oauth1.Token

case class RequestTokenResponse(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = (Configuration.discogs.baseUri / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

object RequestTokenResponse extends FsJsonResponsePipe[RequestTokenResponse]
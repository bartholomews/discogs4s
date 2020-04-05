package io.bartholomews.discogs4s.entities

import cats.effect.Effect
import fs2.Pipe
import fsclient.codecs.FsJsonResponsePipe
import fsclient.implicits.plainTextDecoderPipe
import io.bartholomews.discogs4s.endpoints.AuthorizeUrl
import io.bartholomews.discogs4s.utils.Configuration
import org.http4s.Uri
import org.http4s.client.oauth1.Token

import scala.util.Try

case class RequestToken(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = (Configuration.discogs.baseUri / AuthorizeUrl.path / "authorize")
    .withQueryParam("oauth_token", token.value)
}

object RequestToken extends FsJsonResponsePipe[RequestToken] {
  implicit def plainTextToRequestToken[F[_]: Effect]: Pipe[F, String, RequestToken] =
    plainTextDecoderPipe({
      case Right(s"oauth_token=$token&oauth_token_secret=$secret&oauth_callback_confirmed=$flag") =>
        val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
        RequestToken(Token(token, secret), callbackConfirmed)
    })
}

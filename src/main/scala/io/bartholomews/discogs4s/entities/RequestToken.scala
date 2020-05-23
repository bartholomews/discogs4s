package io.bartholomews.discogs4s.entities

import cats.effect.Effect
import fs2.Pipe
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.fsclient.codecs.FsJsonResponsePipe
import io.bartholomews.fsclient.implicits.plainTextDecoderPipe
import org.http4s.Uri
import org.http4s.client.oauth1.Token

import scala.util.Try

case class RequestToken(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = DiscogsAuthEndpoint.authorizeUri.withQueryParam("oauth_token", token.value)
}

object RequestToken extends FsJsonResponsePipe[RequestToken] {
  implicit def plainTextToRequestToken[F[_]: Effect]: Pipe[F, String, RequestToken] =
    plainTextDecoderPipe({
      case Right(s"oauth_token=$token&oauth_token_secret=$secret&oauth_callback_confirmed=$flag") =>
        val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
        RequestToken(Token(token, secret), callbackConfirmed)
    })
}

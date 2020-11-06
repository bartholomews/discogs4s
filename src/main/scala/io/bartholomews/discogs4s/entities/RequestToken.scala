package io.bartholomews.discogs4s.entities

import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.fsclient.codecs.ResDecoder
import org.http4s.Uri
import org.http4s.client.oauth1.Token

import scala.util.Try

case class RequestToken(token: Token, callbackConfirmed: Boolean) {
  val callback: Uri = DiscogsAuthEndpoint.authorizeUri.withQueryParam("oauth_token", token.value)
}

object RequestToken {
  implicit val plainTextToRequestToken: ResDecoder[String, RequestToken] = {
    case s"oauth_token=$token&oauth_token_secret=$secret&oauth_callback_confirmed=$flag" =>
      val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
      Right(RequestToken(Token(token, secret), callbackConfirmed))

    case other => Left(new Exception(s"Unexpected response: [$other]"))
  }
}

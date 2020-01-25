package io.bartholomews.discogs4s

import cats.effect.IO
import fs2.Pipe
import fsclient.entities.AuthVersion.V1
import fsclient.implicits._
import io.bartholomews.discogs4s.entities.RequestTokenResponse
import org.http4s.client.oauth1.{Consumer, Token}

import scala.util.Try

object DiscogsOAuthPipes {

  implicit val plainTextToRequestTokenResponse: Pipe[IO, String, RequestTokenResponse] =
    plainTextRegexDecoderPipe3("oauth_token=(.*)&oauth_token_secret=(.*)&oauth_callback_confirmed=(.*)".r) {
      (token, secret, flag) =>
        val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
        RequestTokenResponse(Token(token, secret), callbackConfirmed)
    }

  implicit def plainTextToAccessTokenResponse(implicit consumer: Consumer): Pipe[IO, String, V1.AccessToken] =
    plainTextRegexDecoderPipe2("oauth_token=(.*)&oauth_token_secret=(.*)".r) { (token, secret) =>
      V1.AccessToken(Token(token, secret))
    }
}

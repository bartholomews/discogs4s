package client.discogs

import cats.effect.IO
import client.discogs.entities.{AccessTokenResponse, RequestTokenResponse}
import client.effect4s.HttpTypes
import client.effect4s.entities.ResponseError
import org.http4s.Status
import org.http4s.client.oauth1.Token

import scala.util.Try

trait DiscogsOAuthPipes extends HttpTypes {

  private val requestTokenStringResponse = ("oauth_token=(.*)" +
    "&oauth_token_secret=(.*)" +
    "&oauth_callback_confirmed=(.*)"
    ).r

  private val accessTokenStringResponse = "oauth_token=(.*)&oauth_token_secret=(.*)".r
  private val invalidSignature = "Invalid signature"
  private val emptyResponseMessage = "Response was empty, please check request uri"

  private lazy val invalidSignatureError = Left(ResponseError(
    new Exception("Invalid signature. Please double check consumer secret key."),
    Status.Unauthorized
  ))

  implicit val plainTextToRequestTokenResponse: HttpPipe[IO, String, RequestTokenResponse] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {

      case Right(requestTokenStringResponse(token, secret, flag)) =>
        val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
        Right(RequestTokenResponse(Token(token, secret), callbackConfirmed))

      case other => handleInvalidCase(other)
    }

  implicit val plainTextToAccessTokenResponse: HttpPipe[IO, String, AccessTokenResponse] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {
      case Right(accessTokenStringResponse(token, secret)) => Right(AccessTokenResponse(Token(token, secret)))
      case other => handleInvalidCase(other)
    }

  private def handleInvalidCase(either: Either[ResponseError, String]): Either[ResponseError, Nothing] = either match {
    case Right(response) => Left {
      if (response.isEmpty) ResponseError.empty else ResponseError(
        new Exception(s"Unexpected response: $response"),
        Status.BadRequest
      )
    }
    case Left(responseError) =>
      if (responseError.getMessage.startsWith(invalidSignature)) invalidSignatureError
      else Left(responseError)
  }
}

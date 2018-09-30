package client

import cats.effect.IO
import entities.{AccessTokenResponse, RequestTokenResponse, ResponseError}
import org.http4s.Status
import org.http4s.client.oauth1.Token
import utils.HttpResponseUtils

import scala.util.Try

trait OAuthClient extends HttpResponseUtils {

  private val requestTokenStringResponse = ("oauth_token=(.*)" +
    "&oauth_token_secret=(.*)" +
    "&oauth_callback_confirmed=(.*)"
    ).r

  private val accessTokenStringResponse = "oauth_token=(.*)&oauth_token_secret=(.*)".r

  private val invalidSignature = "Invalid signature. (.*)".r
  private val emptyResponseMessage = "Response was empty, please check request uri"

  private def handleInvalidCase(either: Either[ResponseError, String]): Either[ResponseError, Nothing] = either match {

    case Right(invalidSignature(_)) => Left {
      ResponseError(
        new Exception("Invalid signature. Please double check consumer secret key."),
        Status.Unauthorized
      )
    }

    case Right(response) => Left {
      if (response.isEmpty) emptyResponse else ResponseError(
        new Exception(s"Unexpected response: $response"),
        Status.BadRequest
      )
    }

    case Left(responseError) => Left(responseError)
  }

  implicit val plainTextToRequestTokenResponse: PipeTransform[IO, String, RequestTokenResponse] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {

      case Right(requestTokenStringResponse(token, secret, flag)) =>
        val callbackConfirmed = Try(flag.toBoolean).getOrElse(false)
        Right(entities.RequestTokenResponse(Token(token, secret), callbackConfirmed))

      case other => handleInvalidCase(other)
    }

  implicit val plainTextToAccessTokenResponse: PipeTransform[IO, String, AccessTokenResponse] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {

      case Right(accessTokenStringResponse(token, secret)) =>
        Right(AccessTokenResponse(Token(token, secret)))

      case other => handleInvalidCase(other)
    }

}

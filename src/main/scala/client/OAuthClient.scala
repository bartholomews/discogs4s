package client

import cats.effect.IO
import client.api.OAuthResponse
import entities.ResponseError
import org.http4s.Status
import org.http4s.client.oauth1.Token

trait OAuthClient extends IOClient[OAuthResponse] {

  private val oAuthQueryResponse = ("oauth_token=(.*)" +
    "&oauth_token_secret=(.*)" +
    "([&oauth_callback_confirmed=(.*)])?"
    ).r

  private val invalidSignature = "Invalid signature. (.*)".r
  private val emptyResponseMessage = "Response was empty, please check request uri"

  implicit val plainTextToOAuthResponse: PipeTransform[IO, String, OAuthResponse] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {

      case Right(oAuthQueryResponse(token, secret, _)) =>
        Right(OAuthResponse(Token(token, secret)))

      case Right(invalidSignature(_)) => Left {
        ResponseError(
          new Exception("Invalid signature. Please double check consumer secret key."),
          Status.Unauthorized
        )
      }

      case Right(response) => Left {
        if (response.isEmpty) emptyResponse else ResponseError(
          new Exception(response),
          Status.BadRequest
        )
      }

      case Left(responseError) => Left(responseError)
    }

}

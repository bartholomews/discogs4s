package client

import cats.effect.IO
import client.api.AuthorizeUrl
import entities.ResponseError
import org.http4s.{Status, Uri}

trait OAuthClient extends RequestF[Uri] {

  private val oAuthQueryResponse = ("oauth_token=(.*)" +
    "&oauth_token_secret=(.*)" +
    "&oauth_callback_confirmed=(.*)").r

  private val invalidSignature = "Invalid signature. (.*)".r
  private val emptyResponseMessage = "Response was empty, please check request uri"

  implicit val plainTextToUriResponse: PipeTransform[IO, String, Uri] = _
    .last
    .map(_.toLeft(emptyResponseMessage).joinLeft)
    .map {

      case Right(oAuthQueryResponse(token, _, _)) => Right(AuthorizeUrl.response(token))

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

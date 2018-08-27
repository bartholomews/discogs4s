package server

import client.MockClientConfig
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

object ValidateTokenRequestBodyTransformer extends ResponseDefinitionTransformer
  with MockClientConfig with OAuthServer {

  override val applyGlobally = false

  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    implicit val res: ResponseDefinition = response

    def validateVerifier: ResponseDefinition = oAuthResponseHeaders(request) match {
      case requestTokenResponseRegex(a, b, _, _, _, _, _, verifier) =>
        if (verifier == validVerifier) likeResponse(res).build()
        else error(401, ErrorMessage.invalidVerifier)

      case _ => error(401, ErrorMessage.invalidSignature)
    }

    val requestBody = "oauth_token=(.*)&oauth_token_secret=(.*)".r

    response.getBody match {

      case requestBody(token, secret) =>
        if (token != validToken) error(401, ErrorMessage.invalidRequestToken(token))
        else if (secret != validSecret) error(401, ErrorMessage.invalidSignature)
        else validateVerifier

      case _ => error(400, "")
    }
  }

  override def getName: String = "validate-token-request-body-transformer"
}

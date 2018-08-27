package server

import client.MockClientConfig
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

case object AuthenticatedRequestTransformer extends ResponseDefinitionTransformer
  with OAuthServer with MockClientConfig {

  override val applyGlobally = false

  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    implicit val res: ResponseDefinition = response

    oAuthResponseHeaders(request) match {

      case accessTokenResponseRegex(signature, key, _, _, _, _, _) =>

        // TODO verifier is null for requests

        if (key != validConsumerKey) error(400, ErrorMessage.invalidConsumer)
        else {
          // TODO decode signature
          if ("TODO: decode signature" == validConsumerSecret)
            error(400, ErrorMessage.invalidSignature)
          else likeResponse.build()
        }
      case _ => likeResponse.withStatus(400).build()
    }
  }

  override def getName: String = "oauth-request-transformer"
}
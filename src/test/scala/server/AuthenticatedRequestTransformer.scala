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

      case accessTokenResponseRegex(_, key, _, _, _, _, _) =>
        if(key == validConsumerKey) likeResponse.build()
        else if (key == consumerWithInvalidSignature) error(401, ErrorMessage.invalidSignature)
        else if (key == consumerGettingUnexpectedResponse) likeResponse.withBody(unexpectedResponse).build()
        else error(401, ErrorMessage.invalidConsumer)

      case _ => likeResponse.withStatus(400).build()
    }
  }

  override def getName: String = "oauth-request-transformer"
}
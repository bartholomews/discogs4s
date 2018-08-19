package server

import client.MockClientConfig
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

case object AuthenticatedRequestTransformer extends ResponseDefinitionTransformer with MockClientConfig {

  override val applyGlobally = false

  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    val likeResponse = ResponseDefinitionBuilder.like(response).but()

    val reg = (
      "OAuth oauth_signature=\"(.*)\"," +
        "oauth_consumer_key=\"(.*)\"," +
        "oauth_signature_method=\"(.*)\"," +
        "oauth_timestamp=\"(.*)\"," +
        "oauth_nonce=\"(.*)\"," +
        "oauth_version=\"(.*)\"," +
        "oauth_callback=\"(.*)\"" +
        "([,oauth_verifier=\"(.*)\"])?"
      ).r

    val headers = request.getHeader("Authorization")

    headers match {

      case reg(signature, key, _, _, _, _, _, verifier) =>

        println(verifier)

        if (key != validConsumerKey) {
          likeResponse
            .withStatus(400)
            .withBody("Invalid consumer.")
            .build()
        }

        else {

          // TODO decode signature
          if ("TODO: decode signature" == validConsumerSecret) {
            likeResponse
              .withStatus(400)
              .withBody("Invalid signature. " +
                "This additional text shouldn't be shown.")
              .build()
          }

          else likeResponse.build()
        }

      case _ => likeResponse.withStatus(400).build()
    }
  }

  override def getName: String = "oauth-request-transformer"
}
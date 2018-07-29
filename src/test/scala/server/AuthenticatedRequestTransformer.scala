package server

import java.net.URLDecoder

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
        "oauth_callback=\"(.*)\"," +
        "oauth_verifier=\"(.*)\""
      ).r

    request.getHeader("Authorization") match {

      case reg(_, key, _, _, _, _, _, verifier) =>

        if (key != validConsumerKey) {
          likeResponse
            .withStatus(400)
            .withBody("Invalid consumer.")
            .build()
        }

        else {

          import java.nio.charset.StandardCharsets
          import java.util.Base64

          val decoded = Base64.getDecoder.decode(
            URLDecoder.decode(verifier, "UTF-8")
          )
          val secret = new String(decoded, StandardCharsets.UTF_8).split(":")(1)
          if (secret != validConsumerSecret) {
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
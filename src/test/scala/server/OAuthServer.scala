package server

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

import scala.util.matching.Regex

trait OAuthServer {

  def oAuthResponseHeaders(request: Request): String = request.getHeader("Authorization")

  private val oAuthResponseRegex =
    "OAuth oauth_signature=\"(.*)\"," +
      "oauth_consumer_key=\"(.*)\"," +
      "oauth_signature_method=\"(.*)\"," +
      "oauth_timestamp=\"(.*)\"," +
      "oauth_nonce=\"(.*)\"," +
      "oauth_version=\"(.*)\"," +
      "oauth_callback=\"(.*)\""

  val accessTokenResponseRegex: Regex = oAuthResponseRegex.r
  val requestTokenResponseRegex: Regex = (oAuthResponseRegex + ",oauth_verifier=\"(.*)\"").r

  def likeResponse(implicit response: ResponseDefinition): ResponseDefinitionBuilder =
    ResponseDefinitionBuilder.like(response).but()

  def error(code: Int, message: String)(implicit response: ResponseDefinition): ResponseDefinition =
    likeResponse
      .withHeader("Content-Type", "text/plain")
      .withStatus(code)
      .withBody(message)
      .build()

  object ErrorMessage {
    def invalidRequestToken(token: String) = s"Invalid request token: $token"

    val invalidConsumer = "Invalid consumer."
    val invalidSignature = "Invalid signature. This additional text shouldn't be shown."
    val invalidVerifier: String = "Unable to retrieve access token. " +
      "Your request token may have expired."
  }

}

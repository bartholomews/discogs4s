package server

import client.MockClientConfig
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import org.scalatest.words.BehaveWord
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait MockServerWordSpec extends WordSpec with BeforeAndAfterAll with MockClientConfig {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(
      UserAgentHeaderTransformer,
      AuthenticatedRequestTransformer,
      ValidateTokenRequestBodyTransformer,
      ResourceJsonTransformer
    )
  )

  val parsed: BehaveWord = behave

  override def beforeAll: Unit = {
    server.start()
    stubApi()
  }

  override def afterAll: Unit = {
    server.stop()
  }

  private def stubApi(): Unit = {
    stubFor(get(anyUrl())
      .willReturn(aResponse()
        .withStatus(200)
         .withTransformers(defaultTransformers(
           AuthenticatedRequestTransformer, ResourceJsonTransformer
         ): _*)
      ))

    stubFor(get("/empty-response")
      .willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withStatus(200)
      ))

    stubFor(get("/oauth/request_token")
      .willReturn(aResponse()
        .withBody(s"oauth_token=$validToken" +
          s"&oauth_token_secret=$validSecret" +
          s"&oauth_callback_confirmed=true"
        )
        .withTransformers(defaultTransformers(
          AuthenticatedRequestTransformer
        ): _*)
      )
    )

    stubFor(post("/oauth/access_token")
      .willReturn(aResponse()
        .withBody(s"oauth_token=$validToken" +
          s"&oauth_token_secret=$validSecret"
        )
        .withTransformers(defaultTransformers(
          AuthenticatedRequestTransformer,
          ValidateTokenRequestBodyTransformer
        ): _*)
      )
    )
  }

  private def defaultTransformers[T <: ResponseDefinitionTransformer]
  (customTransformers: T*): Seq[String] = Seq.empty ++ customTransformers.map(_.getName)
}
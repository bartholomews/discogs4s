package server

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import org.scalatest.words.BehaveWord
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait MockServerWordSpec extends WordSpec with BeforeAndAfterAll {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(
      UserAgentHeaderTransformer,
      AuthenticatedRequestTransformer,
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
        .withHeader("Content-Type", "application/json")
         .withTransformers(defaultTransformers(ResourceJsonTransformer): _*)
      ))

    stubFor(get("/oauth/request_token")
      .willReturn(aResponse()
        .withHeader("Content-Type", "text/plain")
        .withBody("oauth_token=TOKEN" +
          "&oauth_token_secret=SECRET" +
          "&oauth_callback_confirmed=true"
        )
        .withTransformers(defaultAuthenticatedTransformers: _*)
      )
    )
  }

  private def defaultAuthenticatedTransformers: Seq[String] =
    defaultTransformers(AuthenticatedRequestTransformer)

  private def defaultTransformers[T <: ResponseDefinitionTransformer]
  (customTransformers: T*): Seq[String] = {
    Seq(UserAgentHeaderTransformer.getName) ++ customTransformers.map(_.getName)
  }

}
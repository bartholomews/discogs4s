package server

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.words.BehaveWord
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait MockServerWordSpec extends WordSpec with BeforeAndAfterAll {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(ResourceJsonTransformer)
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
        .withTransformers("resource-json-transformer")
      ))
  }

}
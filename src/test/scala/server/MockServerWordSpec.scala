package server

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterEach, WordSpec}

trait MockServerWordSpec extends WordSpec with BeforeAndAfterEach {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(ResourceJsonTransformer)
  )

  override def beforeEach: Unit = {
    server.start()
    stubApi()
  }

  override def afterEach: Unit = {
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
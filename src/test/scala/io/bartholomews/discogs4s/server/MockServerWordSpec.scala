package io.bartholomews.discogs4s.server

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.bartholomews.discogs4s.wiremock.ResourceFileTransformer
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEachTestData, WordSpec}

trait MockServerWordSpec extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEachTestData {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(ResourceFileTransformer)
  )

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
        .withTransformers(ResourceFileTransformer.getName)
      ))
  }
}
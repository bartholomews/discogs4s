package io.bartholomews.discogs4s.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, anyUrl, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEachTestData, TestData, WordSpec}

trait MockServer extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEachTestData {

  private val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(ResourceFileTransformer)
  )

  private def resetMocks(): Unit =
    server.resetAll()

  override def beforeAll: Unit = {
    server.start()
    resetMocks()
  }

  override def afterAll: Unit =
    server.stop()

  def stubWithResourceFile(): Unit =
    stubFor(
      get(anyUrl())
        .willReturn(
          aResponse()
            .withStatus(200)
            .withTransformers(ResourceFileTransformer.getName)
        )
    )

  override protected def afterEach(testData: TestData): Unit = {
    resetMocks()
    super.afterEach(testData)
  }
}

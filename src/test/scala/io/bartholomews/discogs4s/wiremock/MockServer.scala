package io.bartholomews.discogs4s.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, anyUrl, get, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEachTestData, TestData, WordSpec}

trait MockServer extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEachTestData {

  val server: WireMockServer = new WireMockServer(
    new WireMockConfiguration().extensions(ResourceFileTransformer)
  )

  private def resetMocks(): Unit = {
    server.resetAll()
    stubFor(get(anyUrl())
      .willReturn(aResponse()
        .withStatus(200)
        .withTransformers(ResourceFileTransformer.getName)
      ))
  }

  override def beforeAll: Unit = {
    server.start()
    resetMocks()
  }

  override def afterAll: Unit = {
    server.stop()
  }

  override protected def afterEach(testData: TestData): Unit = {
    resetMocks()
    super.afterEach(testData)
  }
}
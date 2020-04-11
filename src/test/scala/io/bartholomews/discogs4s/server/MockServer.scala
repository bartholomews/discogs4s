package io.bartholomews.discogs4s.server

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, anyUrl, get, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.apache.http.entity.ContentType
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEachTestData, Suite, TestData}

trait MockServer extends BeforeAndAfterAll with BeforeAndAfterEachTestData {

  self: Suite =>

  case class DiscogsError(message: String)

  implicit class ResponseDefinitionImplicits(res: ResponseDefinitionBuilder) {
    def withContentType(contentType: ContentType): ResponseDefinitionBuilder =
      res.withHeader("Content-Type", contentType.getMimeType)
  }

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

  def stubWithResourceFile: StubMapping =
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

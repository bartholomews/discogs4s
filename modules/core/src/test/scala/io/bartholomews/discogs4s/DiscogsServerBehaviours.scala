package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, stubFor}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.discogs4s.client.DiscogsClientData.DiscogsError
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.scalatestudo.{ServerBehaviours, WireWordSpec}
import org.apache.http.entity.ContentType
import org.scalatest.matchers.should.Matchers
import sttp.client3.{DeserializationException, HttpError, Response}
import sttp.model.StatusCode

import scala.reflect.ClassTag

trait DiscogsServerBehaviours[E[_], D[_], DE, J] extends ServerBehaviours[E, D, DE, J] with Matchers {

  self: WireWordSpec =>

  implicit def ct: ClassTag[DE]

  implicit def discogsErrorEncoder: E[DiscogsError]
  implicit def discogsErrorDecoder: D[DiscogsError]

  def clientReceivingUnexpectedResponse[E2, A](
      expectedEndpoint: MappingBuilder,
      request: => SttpResponse[E2, A],
      decodingBody: Boolean = true
  ): Unit = {
    behave.like(clientReceivingUnauthenticatedResponse(expectedEndpoint, request))
    if (decodingBody) behave.like(clientReceivingSuccessfulUnexpectedResponseBody(expectedEndpoint, request))
  }

  private def clientReceivingUnauthenticatedResponse[E2, A](
      expectedEndpoint: MappingBuilder,
      request: => SttpResponse[E2, A]
  ): Unit =
    "the server responds with an error" should {

      def stub: StubMapping =
        stubFor(
          expectedEndpoint
            .willReturn(
              aResponse()
                .withStatus(401)
                .withContentType(ContentType.APPLICATION_JSON)
                .withBodyFile("unauthenticated.json")
            )
        )

      "return a Left with appropriate message" in matchIdResponse(stub, request) {
        case Response(Left(HttpError(body, _)), status, _, _, _, _) =>
          status shouldBe StatusCode.Unauthorized
          val ec = entityCodecs[DiscogsError]
          ec.parse(body).flatMap(ec.decode) shouldBe Right(
            DiscogsError(
              "You must authenticate to access this resource."
            )
          )
      }
    }

  private def clientReceivingSuccessfulUnexpectedResponseBody[E2, A](
      expectedEndpoint: MappingBuilder,
      request: => SttpResponse[E2, A]
  ): Unit =
    "the server response is unexpected" should {

      val ezekiel = """
                      |Ezekiel 25:17.
                      |"The path of the righteous man is beset on all sides
                      |by the inequities of the selfish and the tyranny of evil men.
                      |Blessed is he who, in the name of charity and good will,
                      |shepherds the weak through the valley of the darkness.
                      |For he is truly his brother's keeper and the finder of lost children.
                      |And I will strike down upon thee with great vengeance and furious anger
                      |those who attempt to poison and destroy my brothers.
                      |And you will know I am the Lord
                      |when I lay my vengeance upon you."
                      |""".stripMargin

      def stub: StubMapping =
        stubFor(
          expectedEndpoint
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(ezekiel)
            )
        )

      "return a Left with appropriate message" in matchResponseBody(stub, request) {
        case Left(DeserializationException(body, error)) =>
          body shouldBe ezekiel
          error.isInstanceOf[E2] shouldBe true
      }
    }
}

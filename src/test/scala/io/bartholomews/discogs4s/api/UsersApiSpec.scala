package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.softwaremill.diffx.scalatest.DiffMatcher.matchTo
import io.bartholomews.discogs4s.CoreWireWordSpec
import io.bartholomews.discogs4s.client.ClientData
import io.bartholomews.discogs4s.entities.{SimpleUser, UserLocation, UserRealName, UserWebsite, Username}
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import sttp.client3.{HttpError, Response, UriContext}
import sttp.model.{StatusCode => Status}

class UsersApiSpec extends CoreWireWordSpec {

  import ClientData._

  "getSimpleUserProfile" when {

    def request: SttpResponse[io.circe.Error, SimpleUser] =
      sampleClient.users.getSimpleUserProfile(Username("rodneyfool"))

    "the server responds with the response entity" should {

      "decode the response correctly" in matchResponseBody(stubWithResourceFile, request) {
        case Right(entity) =>
          entity should matchTo(
            SimpleUser(
              profile = "I am a software developer for Discogs.\r\n\r\n[img=http://i.imgur.com/IAk3Ukk.gif]",
              wantlistUrl = uri"https://api.discogs.com/users/rodneyfool/wants",
              rank = 149L,
              numPending = 61L,
              id = 1578108L,
              numForSale = 0L,
              homePage = UserWebsite(""),
              location = UserLocation("I live in the good ol' Pacific NW"),
              collectionFoldersUrl = uri"https://api.discogs.com/users/rodneyfool/collection/folders",
              username = Username("rodneyfool"),
              collectionFieldsUrl = uri"https://api.discogs.com/users/rodneyfool/collection/fields",
              releasesContributed = 5L,
              registered = "2012-08-15T21:13:36-07:00", // FIXME date
              ratingAvg = 3.47,
              numCollection = Some(78L),
              releasesRated = 116L,
              numLists = 0L,
              name = UserRealName("Rodney"),
              numWantlist = Some(160L),
              inventoryUrl = uri"https://api.discogs.com/users/rodneyfool/inventory",
              avatarUrl = uri"http://www.gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0?s=52&r=pg&d=mm",
              bannerUrl = Some(
                uri"https://img.discogs.com/dhuJe-pRJmod7hN3cdVi2PugEh4=/1600x400/filters:strip_icc():format(jpeg)/discogs-banners/B-1578108-user-1436314164-9231.jpg.jpg"
              ),
              uri = uri"http://www.discogs.com/user/rodneyfool",
              resourceUrl = uri"https://api.discogs.com/users/rodneyfool",
              buyerRating = 100.00,
              buyerRatingStars = 5,
              buyerNumRatings = 144L,
              sellerRatingStars = 5,
              sellerNumRatings = 21L,
              currAbbr = "USD"
            )
          )
      }
    }

    "the server responds with an error" should {

      def stub: StubMapping =
        stubFor(
          get(urlPathEqualTo("/users/rodneyfool"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withBody("Invalid consumer.")
            )
        )

      "decode an `Unauthorized` response" in matchIdResponse(stub, request) {
        case Response(Left(HttpError(body, statusCode)), status, _, _, _, _) =>
          status shouldBe Status.Unauthorized
          statusCode shouldBe Status.Unauthorized
          body shouldBe "Invalid consumer."
      }
    }
  }
}

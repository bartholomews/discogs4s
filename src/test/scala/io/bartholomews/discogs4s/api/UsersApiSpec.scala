package io.bartholomews.discogs4s.api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.bartholomews.fsclient.entities.{FsResponseErrorString, FsResponseSuccess}
import io.bartholomews.fsclient.utils.HttpTypes.IOResponse
import io.bartholomews.discogs4s.client.MockClient
import io.bartholomews.discogs4s.entities.{SimpleUser, UserLocation, UserRealName, UserWebsite, Username}
import io.bartholomews.testudo.WireWordSpec
import org.http4s.{Status, Uri}

class UsersApiSpec extends WireWordSpec with MockClient {

  "getSimpleUserProfile" when {

    def request: IOResponse[SimpleUser] =
      sampleClient.users.getSimpleUserProfile(Username("rodneyfool"))

    "the server responds with the response entity" should {

      "decode the response correctly" in matchResponse(stubWithResourceFile, request) {
        case FsResponseSuccess(_, _, entity) =>
          entity should matchTo(
            SimpleUser(
              profile = "I am a software developer for Discogs.\r\n\r\n[img=http://i.imgur.com/IAk3Ukk.gif]",
              wantlistUrl = Uri.unsafeFromString("https://api.discogs.com/users/rodneyfool/wants"),
              rank = 149L,
              numPending = 61L,
              id = 1578108L,
              numForSale = 0L,
              homePage = UserWebsite(""),
              location = UserLocation("I live in the good ol' Pacific NW"),
              collectionFoldersUrl = Uri.unsafeFromString("https://api.discogs.com/users/rodneyfool/collection/folders"),
              username = Username("rodneyfool"),
              collectionFieldsUrl = Uri.unsafeFromString("https://api.discogs.com/users/rodneyfool/collection/fields"),
              releasesContributed = 5L,
              registered = "2012-08-15T21:13:36-07:00", // FIXME date
              ratingAvg = 3.47,
              numCollection = Some(78L),
              releasesRated = 116L,
              numLists = 0L,
              name = UserRealName("Rodney"),
              numWantlist = Some(160L),
              inventoryUrl = Uri.unsafeFromString("https://api.discogs.com/users/rodneyfool/inventory"),
              avatarUrl =
                Uri.unsafeFromString("http://www.gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0?s=52&r=pg&d=mm"),
              bannerUrl = Uri.unsafeFromString(
                "https://img.discogs.com/dhuJe-pRJmod7hN3cdVi2PugEh4=/1600x400/filters:strip_icc():format(jpeg)/discogs-banners/B-1578108-user-1436314164-9231.jpg.jpg"
              ),
              uri = Uri.unsafeFromString("http://www.discogs.com/user/rodneyfool"),
              resourceUrl = Uri.unsafeFromString("https://api.discogs.com/users/rodneyfool"),
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

      "decode an `Unauthorized` response" in matchResponse(stub, request) {
        case FsResponseErrorString(_, status, error) =>
          status shouldBe Status.Unauthorized
          error shouldBe "Invalid consumer."
      }
    }
  }
}

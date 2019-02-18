package client.discogs.entities

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveDecoder

// https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
case class SimpleUser(
                       profile: String,
                       wantlistUrl: String, // FIXME Uri
                       rank: Long,
                       numPending: Long,
                       id: Long,
                       numForSale: Long,
                       homePage: String,
                       location: String,
                       collectionFoldersUrl: String, // FIXME Uri
                       username: String,
                       collectionFieldsUrl: String, // FIXME Uri
                       releasesContributed: Long,
                       registered: String, // FIXME date
                       ratingAvg: Long,
                       releaseRated: Option[Long], // FIXME is it returned in some cases or should it be removed?
                       numLists: Long,
                       name: String,
                       inventoryUrl: String, // FIXME Uri
                       avatarUrl: String, // FIXME Uri
                       bannerUrl: String, // FIXME Uri
                       uri: String, // FIXME Uri
                       resourceUrl: String, // FIXME Uri
                       buyerRating: BigDecimal,
                       buyerRatingStars: Short,
                       buyerNumRating: Option[BigDecimal], // FIXME is it returned in some cases or should it be removed?
                       sellerRatingStars: Short,
                       sellerNumRatings: Long,
                       currAbbr: String) extends DiscogsEntity

object SimpleUser {
  implicit val decoder: Decoder[SimpleUser] = deriveDecoder[SimpleUser]
}

case class AuthenticatedUser(
                              profile: String,
                              wantlistUrl: String, // FIXME Uri
                              rank: Long,
                              numPending: Long,
                              id: Long,
                              numForSale: Long,
                              homePage: String,
                              location: String,
                              collectionFoldersUrl: String, // FIXME Uri
                              username: String,
                              collectionFieldsUrl: String, // FIXME Uri
                              releasesContributed: Long,
                              registered: String, // FIXME date
                              ratingAvg: Long,
                              numCollection: Long,
                              releaseRated: Option[Long], // FIXME is it returned in some cases or should it be removed?
                              numLists: Long,
                              name: String,
                              numWantlist: Long,
                              inventoryUrl: String, // FIXME Uri
                              avatarUrl: String, // FIXME Uri
                              bannerUrl: String, // FIXME Uri
                              uri: String, // FIXME Uri
                              resourceUrl: String, // FIXME Uri
                              buyerRating: BigDecimal,
                              buyerRatingStars: Short,
                              buyerNumRating: Option[BigDecimal], // FIXME is it returned in some cases or should it be removed?
                              sellerRatingStars: Short,
                              sellerNumRatings: Long,
                              currAbbr: String) extends DiscogsEntity

object AuthenticatedUser {
  implicit val decoder: Decoder[AuthenticatedUser] = deriveDecoder[AuthenticatedUser]
}
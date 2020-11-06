package io.bartholomews.discogs4s.entities

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import org.http4s.Uri

// https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
case class SimpleUser(
  profile: String,
  wantlistUrl: Uri,
  rank: Long,
  numPending: Long,
  id: Long,
  numForSale: Long,
  homePage: UserWebsite,
  location: UserLocation,
  collectionFoldersUrl: Uri,
  username: Username,
  collectionFieldsUrl: Uri,
  releasesContributed: Long,
  registered: String, // FIXME date
  ratingAvg: Double,
  numCollection: Option[Long],
  releasesRated: Long,
  numLists: Long,
  name: UserRealName,
  numWantlist: Option[Long],
  inventoryUrl: Uri,
  avatarUrl: Uri,
  bannerUrl: Uri,
  uri: Uri,
  resourceUrl: Uri,
  buyerRating: Double,
  buyerRatingStars: Short,
  buyerNumRatings: Long,
  sellerRatingStars: Short,
  sellerNumRatings: Long,
  currAbbr: String // FIXME [is this a COUNTRY ISO CODE? Should probably decode as Option[IsoDate]]
) extends DiscogsEntity

object SimpleUser {
  implicit val decoder: Decoder[SimpleUser] = deriveConfiguredDecoder[SimpleUser]
}

case class AuthenticatedUser(
  profile: String,
  wantlistUrl: Uri,
  rank: Long,
  numPending: Long,
  num_unread: Long,
  id: Long,
  numForSale: Long,
  homePage: UserWebsite,
  location: UserLocation,
  collectionFoldersUrl: Uri,
  username: Username,
  collectionFieldsUrl: Uri,
  releasesContributed: Long,
  registered: String, // FIXME date
  ratingAvg: Double,
  numCollection: Long,
  releasesRated: Long,
  numLists: Long,
  name: UserRealName,
  email: UserEmail,
  numWantlist: Long,
  inventoryUrl: Uri,
  avatarUrl: Uri,
  bannerUrl: Uri,
  uri: Uri,
  resourceUrl: Uri,
  buyerRating: Double,
  buyerRatingStars: Short,
  buyerNumRatings: Long,
  sellerRatingStars: Short,
  sellerNumRatings: Long,
  currAbbr: String // FIXME [is this a COUNTRY ISO CODE? Should probably decode a [IsoDate]
) extends DiscogsEntity

object AuthenticatedUser {
  implicit val decoder: Decoder[AuthenticatedUser] = deriveConfiguredDecoder[AuthenticatedUser]
}

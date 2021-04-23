package io.bartholomews.discogs4s.entities

import sttp.model.Uri

// https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
final case class SimpleUser(
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
  bannerUrl: Option[Uri],
  uri: Uri,
  resourceUrl: Uri,
  buyerRating: Double,
  buyerRatingStars: Short,
  buyerNumRatings: Long,
  sellerRatingStars: Short,
  sellerNumRatings: Long,
  currAbbr: String // FIXME [is this a COUNTRY ISO CODE? Should probably decode as Option[IsoDate]]
)

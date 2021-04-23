package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class AuthenticatedUser(
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
)

package io.bartholomews.discogs4s.entities

import java.time.LocalDateTime

import sttp.model.Uri

// https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
final case class UserProfile(
    id: DiscogsUserId,
    profile: DiscogsUserProfileInfo,
    email: Option[DiscogsUserEmail],
    rank: Long,
    wantlistUrl: Uri,
    numForSale: Int,
    numPending: Int,
    numCollection: Option[Int],
    numUnread: Option[Int],
    numWantlist: Option[Int],
    homePage: DiscogsUserWebsite,
    location: DiscogsUserLocation,
    collectionFoldersUrl: Uri,
    username: DiscogsUsername,
    collectionFieldsUrl: Uri,
    releasesContributed: Long,
    registered: LocalDateTime,
    ratingAvg: Double,
    releasesRated: Long,
    numLists: Long,
    name: DiscogsUserRealName,
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
    currAbbr: MarketplaceCurrency
)

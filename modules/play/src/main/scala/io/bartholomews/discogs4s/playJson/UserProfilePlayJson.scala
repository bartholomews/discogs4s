package io.bartholomews.discogs4s.playJson

import io.bartholomews.discogs4s.entities._
import play.api.libs.json._
import sttp.model.Uri

object UserProfilePlayJson {

  import codecs._

  val reads: Reads[UserProfile] = { (json: JsValue) =>
    for {
      profile              <- (json \ "profile").validate[String]
      wantlistUrl          <- (json \ "wantlist_url").validate[Uri]
      rank                 <- (json \ "rank").validate[Long]
      numPending           <- (json \ "num_pending").validate[Int]
      numUnread           <- (json \ "num_unread").validateOpt[Int]
      id                   <- (json \ "id").validate[Long]
      numForSale           <- (json \ "num_for_sale").validate[Int]
      homePage             <- (json \ "home_page").validate[UserWebsite]
      location             <- (json \ "location").validate[UserLocation]
      collectionFoldersUrl <- (json \ "collection_folders_url").validate[Uri]
      username             <- (json \ "username").validate[Username]
      collectionFieldsUrl  <- (json \ "collection_fields_url").validate[Uri]
      releasesContributed  <- (json \ "releases_contributed").validate[Long]
      registered           <- (json \ "registered").validate[String]
      ratingAvg            <- (json \ "rating_avg").validate[Double]
      numCollection        <- (json \ "num_collection").validateOpt[Int]
      releasesRated        <- (json \ "releases_rated").validate[Long]
      numLists             <- (json \ "num_lists").validate[Long]
      name                 <- (json \ "name").validate[UserRealName]
      email                <- (json \ "email").validateOpt[UserEmail]
      numWantlist          <- (json \ "num_wantlist").validateOpt[Int]
      inventoryUrl         <- (json \ "inventory_url").validate[Uri]
      avatarUrl            <- (json \ "avatar_url").validate[Uri]
      bannerUrl            <- jsResultEmptyStringAsNone[Uri](json \ "banner_url")
      uri                  <- (json \ "uri").validate[Uri]
      resourceUrl          <- (json \ "resource_url").validate[Uri]
      buyerRating          <- (json \ "buyer_rating").validate[Double]
      buyerRatingStars     <- (json \ "buyer_rating_stars").validate[Short]
      buyerNumRatings      <- (json \ "buyer_num_ratings").validate[Long]
      sellerRatingStars    <- (json \ "seller_rating_stars").validate[Short]
      sellerNumRatings     <- (json \ "seller_num_ratings").validate[Long]
      currAbbr             <- (json \ "curr_abbr").validate[String]
    } yield UserProfile(
      id,
      profile,
      email,
      rank,
      wantlistUrl,
      numForSale,
      numPending,
      numCollection,
      numUnread,
      numWantlist,
      homePage,
      location,
      collectionFoldersUrl,
      username,
      collectionFieldsUrl,
      releasesContributed,
      registered,
      ratingAvg,
      releasesRated,
      numLists,
      name,
      inventoryUrl,
      avatarUrl,
      bannerUrl,
      uri,
      resourceUrl,
      buyerRating,
      buyerRatingStars,
      buyerNumRatings,
      sellerRatingStars,
      sellerNumRatings,
      currAbbr
    )
  }
}

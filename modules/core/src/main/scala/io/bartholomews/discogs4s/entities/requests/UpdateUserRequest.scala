package io.bartholomews.discogs4s.entities.requests

import io.bartholomews.discogs4s.entities._

final case class UpdateUserRequest(
    name: Option[DiscogsUserRealName],
    homePage: Option[DiscogsUserWebsite],
    location: Option[DiscogsUserLocation],
    profile: Option[DiscogsUserProfileInfo],
    currAbbr: Option[MarketplaceCurrency]
)

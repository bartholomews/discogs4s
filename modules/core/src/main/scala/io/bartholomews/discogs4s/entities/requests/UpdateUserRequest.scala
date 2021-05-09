package io.bartholomews.discogs4s.entities.requests

import io.bartholomews.discogs4s.entities._

final case class UpdateUserRequest(
    name: Option[UserRealName],
    homePage: Option[UserWebsite],
    location: Option[UserLocation],
    profile: Option[UserProfileInfo],
    currAbbr: Option[MarketplaceCurrency]
)

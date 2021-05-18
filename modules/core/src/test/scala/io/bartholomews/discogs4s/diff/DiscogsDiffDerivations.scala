package io.bartholomews.discogs4s.diff

import java.time.LocalDateTime

import com.softwaremill.diffx.Diff
import io.bartholomews.discogs4s.entities._
import io.bartholomews.scalatestudo.diff.DiffDerivations

trait DiscogsDiffDerivations extends DiffDerivations {
  //
  implicit val localDateTimeDiff: Diff[LocalDateTime] = Diff.useEquals

  implicit val marketplaceCurrencyDiff: Diff[MarketplaceCurrency] =
    Diff.diffForString.contramap[MarketplaceCurrency](_.entryName)

  implicit val pageUrlsDiff: Diff[PageUrls]                             = Diff.derived[PageUrls]
  implicit val releaseDiff: Diff[ArtistReleaseSubmission]               = Diff.derived[ArtistReleaseSubmission]
  implicit val paginationDiff: Diff[Pagination]                         = Diff.derived[Pagination]
  implicit val paginatedReleasesDiff: Diff[PaginatedReleases]           = Diff.derived[PaginatedReleases]
  implicit val usernameDiff: Diff[DiscogsUsername]                      = Diff.derived[DiscogsUsername]
  implicit val discogsUserIdDiff: Diff[DiscogsUserId]                   = Diff.derived[DiscogsUserId]
  implicit val discogsUserEmailDiff: Diff[DiscogsUserEmail]             = Diff.derived[DiscogsUserEmail]
  implicit val discogsUserRealNameDiff: Diff[DiscogsUserRealName]       = Diff.derived[DiscogsUserRealName]
  implicit val discogsUserWebsiteDiff: Diff[DiscogsUserWebsite]         = Diff.derived[DiscogsUserWebsite]
  implicit val discogsUserLocationDiff: Diff[DiscogsUserLocation]       = Diff.derived[DiscogsUserLocation]
  implicit val discogsUserResourceDiff: Diff[DiscogsUserResource]       = Diff.derived[DiscogsUserResource]
  implicit val discogsUserProfileInfoDiff: Diff[DiscogsUserProfileInfo] = Diff.derived[DiscogsUserProfileInfo]
  implicit val userProfileDiff: Diff[UserProfile]                       = Diff.derived[UserProfile]
}

object DiscogsDiffDerivations extends DiscogsDiffDerivations

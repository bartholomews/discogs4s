package io.bartholomews.discogs4s.diff

import com.softwaremill.diffx.Diff
import io.bartholomews.discogs4s.entities._
import io.bartholomews.scalatestudo.diff.DiffDerivations

import java.time.LocalDateTime

trait DiscogsDiffDerivations extends DiffDerivations {
  //
  implicit val localDateTimeDiff: Diff[LocalDateTime] = Diff.useEquals

  implicit val marketplaceCurrencyDiff: Diff[MarketplaceCurrency] =
    Diff.diffForString.contramap[MarketplaceCurrency](_.entryName)

  implicit val pageUrlsDiff: Diff[PageUrls]                             = Diff.derived[PageUrls]
  implicit val releaseStatusDiff: Diff[ReleaseStatus]                   = Diff.derived[ReleaseStatus]
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

  implicit val discogsReleaseIdDiff: Diff[DiscogsReleaseId]           = Diff.derived[DiscogsReleaseId]
  implicit val ratingAverageDiff: Diff[RatingAverage]                 = Diff.derived[RatingAverage]
  implicit val communityReleaseDiff: Diff[CommunityRelease]           = Diff.derived[CommunityRelease]
  implicit val communityReleaseStatsDiff: Diff[CommunityReleaseStats] = Diff.derived[CommunityReleaseStats]

  implicit val genreDiff: Diff[Genre]                   = Diff.derived[Genre]
  implicit val styleDiff: Diff[Style]                   = Diff.derived[Style]
  implicit val releaseVersionDiff: Diff[ReleaseVersion] = Diff.derived[ReleaseVersion]
  implicit val releaseStatsDiff: Diff[ReleaseStats]     = Diff.derived[ReleaseStats]
  implicit val masterIdDiff: Diff[MasterId]             = Diff.derived[MasterId]

  implicit val releaseVideoDiff: Diff[ReleaseVideo]   = Diff.derived[ReleaseVideo]
  implicit val artistReleaseDiff: Diff[ArtistRelease] = Diff.derived[ArtistRelease]
  implicit val releaseTrackDiff: Diff[ReleaseTrack]   = Diff.derived[ReleaseTrack]

  implicit val releaseFilterFormatDiff: Diff[ReleaseFilter.Format] =
    Diff.derived[ReleaseFilter.Format]
  implicit val releaseFilterLabelDiff: Diff[ReleaseFilter.Label] =
    Diff.derived[ReleaseFilter.Label]
  implicit val releaseFilterCountryDiff: Diff[ReleaseFilter.Country] =
    Diff.derived[ReleaseFilter.Country]
  implicit val releaseFilterReleasedYearDiff: Diff[ReleaseFilter.ReleasedYear] =
    Diff.derived[ReleaseFilter.ReleasedYear]

  implicit val availableFiltersDiff: Diff[AvailableFilters]           = Diff.derived[AvailableFilters]
  implicit val appliedFiltersDiff: Diff[AppliedFilters]               = Diff.derived[AppliedFilters]
  implicit val filtersInfoDiff: Diff[FiltersInfo]                     = Diff.derived[FiltersInfo]
  implicit val filterFacetValueDiff: Diff[FilterFacetValue]           = Diff.derived[FilterFacetValue]
  implicit val filterFacetDiff: Diff[FilterFacet]                     = Diff.derived[FilterFacet]
  implicit val releaseImageDiff: Diff[ReleaseImage]                   = Diff.derived[ReleaseImage]
  implicit val masterReleaseDiff: Diff[MasterRelease]                 = Diff.derived[MasterRelease]
  implicit val masterReleaseVersionsDiff: Diff[MasterReleaseVersions] = Diff.derived[MasterReleaseVersions]
}

object DiscogsDiffDerivations extends DiscogsDiffDerivations

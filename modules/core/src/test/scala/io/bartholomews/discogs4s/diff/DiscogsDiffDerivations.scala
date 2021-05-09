package io.bartholomews.discogs4s.diff

import com.softwaremill.diffx.Diff
import io.bartholomews.discogs4s.entities._
import io.bartholomews.scalatestudo.diff.DiffDerivations

trait DiscogsDiffDerivations extends DiffDerivations {
  implicit val pageUrlsDiff: Diff[PageUrls]                   = Diff.derived[PageUrls]
  implicit val releaseDiff: Diff[ArtistReleaseSubmission]                     = Diff.derived[ArtistReleaseSubmission]
  implicit val paginationDiff: Diff[Pagination]               = Diff.derived[Pagination]
  implicit val paginatedReleasesDiff: Diff[PaginatedReleases] = Diff.derived[PaginatedReleases]
  implicit val usernameDiff: Diff[Username]                   = Diff.derived[Username]
  implicit val userEmailDiff: Diff[UserEmail]                 = Diff.derived[UserEmail]
  implicit val userRealNameDiff: Diff[UserRealName]           = Diff.derived[UserRealName]
  implicit val userWebsiteDiff: Diff[UserWebsite]             = Diff.derived[UserWebsite]
  implicit val userLocationDiff: Diff[UserLocation]           = Diff.derived[UserLocation]
  implicit val userResourceDiff: Diff[UserResource]           = Diff.derived[UserResource]
  implicit val userProfileDiff: Diff[UserProfile]             = Diff.derived[UserProfile]
}

object DiscogsDiffDerivations extends DiscogsDiffDerivations

package io.bartholomews.discogs4s.diff

import com.softwaremill.diffx.Diff
import io.bartholomews.discogs4s.entities._
import io.bartholomews.scalatestudo.diff.DiffDerivations

trait DiscogsDiffDerivations extends DiffDerivations {
  implicit val pageUrlsDiff: Diff[PageUrls] = Diff.derived[PageUrls]
  implicit val releaseDiff: Diff[Release] = Diff.derived[Release]
  implicit val paginationDiff: Diff[Pagination] = Diff.derived[Pagination]
  implicit val paginatedReleasesDiff: Diff[PaginatedReleases] = Diff.derived[PaginatedReleases]
  implicit val userWebsiteDiff: Diff[UserWebsite] = Diff.derived[UserWebsite]
  implicit val userLocationDiff: Diff[UserLocation] = Diff.derived[UserLocation]
  implicit val usernameDiff: Diff[Username] = Diff.derived[Username]
  implicit val userRealNameDiff: Diff[UserRealName] = Diff.derived[UserRealName]
  implicit val simpleUserDiff: Diff[SimpleUser] = Diff.derived[SimpleUser]
}

object DiscogsDiffDerivations extends DiscogsDiffDerivations

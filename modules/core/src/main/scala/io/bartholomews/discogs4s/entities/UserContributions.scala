package io.bartholomews.discogs4s.entities

import java.time.LocalDateTime

import sttp.model.Uri

final case class UserContributions(
    pagination: Pagination,
    contributions: List[ReleaseSubmission]
)

object UserContributions {
  import enumeratum.EnumEntry.Lowercase
  import enumeratum._

  sealed trait SortedBy extends EnumEntry with Lowercase

  object SortedBy extends Enum[SortedBy] {

    override val values: IndexedSeq[SortedBy] = findValues

    case object LABEL  extends SortedBy
    case object ARTIST extends SortedBy
    case object TITLE  extends SortedBy
    case object CATNO  extends SortedBy
    case object FORMAT extends SortedBy
    case object RATING extends SortedBy
    case object YEAR   extends SortedBy
    case object ADDED  extends SortedBy
  }
}

final case class ReleaseSubmission(
    artists: List[ArtistReleaseSubmission],
    community: CommunityReleaseSubmission,
    country: Option[String],
    dataQuality: String,
    dateAdded: LocalDateTime,
    dateChanged: LocalDateTime,
    estimatedWeight: Option[Int],
    formatQuantity: Int,
    formats: List[ReleaseFormat],
    genres: List[String],
    id: Long,
    images: List[ReleaseImage],
    labels: List[ReleaseLabel],
    masterId: Option[Long],
    masterUrl: Option[String],
    notes: Option[String],
    released: Option[String],
    releasedFormatted: Option[String],
    resourceUrl: Uri,
    status: String,
    styles: List[Style],
    thumb: Uri,
    title: String,
    uri: Uri,
    videos: List[ReleaseVideo],
    year: Int
)

final case class Style(value: String) extends AnyVal

final case class ReleaseVideo(description: String, duration: Double, embed: Boolean, title: String, uri: Uri)
final case class ReleaseImage(height: Int, resourceUrl: Uri, `type`: String, uri: Uri, uri150: Uri, width: Int)
final case class ReleaseLabel(catno: String, entityType: String, id: Long, name: String, resourceUrl: Uri)
final case class ReleaseFormat(descriptions: List[FormatDescription], name: String, qty: Int)

final case class FormatDescription(value: String) extends AnyVal

final case class ArtistReleaseSubmission(
    anv: String,
    id: Long,
    join: String,
    name: String,
    resourceUrl: Uri,
    role: String,
    tracks: String
)

final case class CommunityReleaseSubmission(
    contributors: List[UserResource],
    dataQuality: String,
    have: Int,
    rating: Rating,
    status: String,
    submitter: UserResource,
    want: Int
)

final case class Rating(average: Double, count: Int)

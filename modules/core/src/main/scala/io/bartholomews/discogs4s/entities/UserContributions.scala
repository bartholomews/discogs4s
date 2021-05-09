package io.bartholomews.discogs4s.entities

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

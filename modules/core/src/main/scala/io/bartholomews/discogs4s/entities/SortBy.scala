package io.bartholomews.discogs4s.entities

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait SortBy extends EnumEntry with Lowercase

object SortBy extends Enum[SortBy] {

  override val values: IndexedSeq[SortBy] = findValues

  case object Year extends SortBy
  case object Title extends SortBy
  case object Format extends SortBy
}

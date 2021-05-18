package io.bartholomews.discogs4s.entities

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait SortOrder extends EnumEntry with Lowercase

object SortOrder extends Enum[SortOrder] {

  override val values: IndexedSeq[SortOrder] = findValues

  case object Asc  extends SortOrder
  case object Desc extends SortOrder
}

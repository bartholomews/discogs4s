package io.bartholomews.discogs4s.entities

import enumeratum.EnumEntry.Uppercase
import enumeratum._

sealed trait MarketplaceCurrency extends EnumEntry with Uppercase

object MarketplaceCurrency extends Enum[MarketplaceCurrency] {

  override val values: IndexedSeq[MarketplaceCurrency] = findValues

  case object USD extends MarketplaceCurrency
  case object GBP extends MarketplaceCurrency
  case object EUR extends MarketplaceCurrency
  case object CAD extends MarketplaceCurrency
  case object AUD extends MarketplaceCurrency
  case object JPY extends MarketplaceCurrency
  case object CHF extends MarketplaceCurrency
  case object MXN extends MarketplaceCurrency
  case object BRL extends MarketplaceCurrency
  case object NZD extends MarketplaceCurrency
  case object SEK extends MarketplaceCurrency
  case object ZAR extends MarketplaceCurrency
}

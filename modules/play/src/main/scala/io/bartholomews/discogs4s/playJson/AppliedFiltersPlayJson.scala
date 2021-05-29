package io.bartholomews.discogs4s.playJson

import io.bartholomews.discogs4s.entities._
import play.api.libs.json._

private[playJson] object AppliedFiltersPlayJson {

  import codecs._

  private val writes: Writes[AppliedFilters] = Json.writes
  private val reads: Reads[AppliedFilters] = { (json: JsValue) =>
    for {
      format   <- (json \ "format").validateOpt[List[ReleaseFilter.Format]].map(_.getOrElse(Nil))
      label    <- (json \ "label").validateOpt[List[ReleaseFilter.Label]].map(_.getOrElse(Nil))
      country  <- (json \ "country").validateOpt[List[ReleaseFilter.Country]].map(_.getOrElse(Nil))
      released <- (json \ "released").validateOpt[List[ReleaseFilter.ReleasedYear]].map(_.getOrElse(Nil))
    } yield AppliedFilters(
      `format`,
      label,
      country,
      released
    )
  }

  val codec: Format[AppliedFilters] = Format.apply(reads, writes)
}

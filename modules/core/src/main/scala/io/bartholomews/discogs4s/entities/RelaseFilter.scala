package io.bartholomews.discogs4s.entities

final case class FiltersInfo(
    available: AvailableFilters,
    applied: AppliedFilters
)

final case class AppliedFilters(
    format: List[ReleaseFilter.Format],
    label: List[ReleaseFilter.Label],
    country: List[ReleaseFilter.Country],
    released: List[ReleaseFilter.ReleasedYear]
)

final case class AvailableFilters(
    format: Map[String, Int],
    label: Map[String, Int],
    country: Map[String, Int],
    released: Map[String, Int]
)

sealed trait ReleaseFilter
object ReleaseFilter {
  final case class Format(value: String)       extends ReleaseFilter
  final case class Label(value: String)        extends ReleaseFilter
  final case class ReleasedYear(value: String) extends ReleaseFilter
  final case class Country(value: String)      extends ReleaseFilter
}

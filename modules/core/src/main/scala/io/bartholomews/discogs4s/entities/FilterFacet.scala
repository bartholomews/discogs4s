package io.bartholomews.discogs4s.entities

final case class FilterFacet(
    title: String,
    id: String,
    values: List[FilterFacetValue]
)

final case class FilterFacetValue(title: String, value: String, count: Int)

package io.bartholomews.discogs4s.entities

final case class MasterReleaseVersions(
    pagination: Pagination,
    filters: FiltersInfo,
    filterFacets: List[FilterFacet],
    versions: List[ReleaseVersion]
)

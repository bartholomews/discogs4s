package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class ReleaseVersion(
    id: DiscogsReleaseId,
    label: ReleaseFilter.Label,
    country: ReleaseFilter.Country,
    title: String,
    majorFormats: List[ReleaseFilter.Format],
    catno: String,
    released: ReleaseFilter.ReleasedYear,
    status: ReleaseStatus,
    resourceUrl: Uri,
    thumb: String,
    stats: ReleaseStats
)

// FIXME: CommunityReleaseStats has in_wantlist/in_collection keys
final case class ReleaseStats(community: CommunityReleaseStats)

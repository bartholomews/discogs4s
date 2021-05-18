package io.bartholomews.discogs4s.entities

final case class CommunityReleaseSubmission(
    contributors: List[DiscogsUserResource],
    dataQuality: String,
    have: Int,
    rating: Rating,
    status: String,
    submitter: DiscogsUserResource,
    want: Int
)

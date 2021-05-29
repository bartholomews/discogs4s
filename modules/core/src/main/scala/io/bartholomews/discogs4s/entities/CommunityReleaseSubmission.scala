package io.bartholomews.discogs4s.entities

final case class CommunityReleaseSubmission(
    contributors: List[DiscogsUserResource],
    dataQuality: String,
    have: Int,
    rating: RatingAverage,
    status: ReleaseStatus,
    submitter: DiscogsUserResource,
    want: Int
)

final case class CommunityRelease(releaseId: DiscogsReleaseId, rating: RatingAverage)
final case class CommunityReleaseStats(
    numHave: Option[Int],
    numWant: Option[Int],
    isOffensive: Option[Boolean]
)

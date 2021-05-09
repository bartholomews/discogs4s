package io.bartholomews.discogs4s.entities

final case class CommunityReleaseSubmission(
    contributors: List[UserResource],
    dataQuality: String,
    have: Int,
    rating: Rating,
    status: String,
    submitter: UserResource,
    want: Int
)

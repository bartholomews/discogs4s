package io.bartholomews.discogs4s.entities

final case class CommunityReleaseSubmission(
                                             contributors: List[DiscogsUserResource],
                                             dataQuality: String,
                                             have: Int,
                                             rating: RatingAverage,
                                             status: String,
                                             submitter: DiscogsUserResource,
                                             want: Int
)

package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class UserSubmissionResponse(pagination: Pagination, submissions: UserSubmissions)

final case class UserSubmissions(
    artists: List[ArtistSubmission],
    releases: List[ReleaseSubmission]
)

final case class ArtistSubmission(
    dataQuality: String,
    id: Long,
    name: String,
    namevariations: List[String],
    releasesUrl: Uri,
    resourceUrl: Uri,
    uri: Uri
)

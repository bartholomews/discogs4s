package io.bartholomews.discogs4s.entities

import java.time.LocalDateTime

import sttp.model.Uri

final case class Release(
    id: DiscogsReleaseId,
    status: ReleaseStatus,
    year: Int,
    resourceUrl: Uri,
    uri: Uri,
    artists: List[ArtistRelease],
    artistsSort: String,
    labels: List[EntityResource],
    series: List[EntityResource],
    companies: List[EntityResource],
    formats: List[ReleaseFormat],
    dataQuality: String,
    community: CommunityReleaseSubmission,
    formatQuantity: Int,
    dateAdded: LocalDateTime,
    dateChanged: LocalDateTime,
    numForSale: Int,
    lowestPrice: BigDecimal,
    masterId: Long,
    masterUrl: Uri,
    title: String,
    country: String,
    // TODO: not `LocalDate` as it seems to use invalid format (maybe to signal unknown fragments, e.g. "1987-07-00")
    //  could create a more complex type with variable precision
    released: String,
    notes: String,
    releasedFormatted: String,
    identifiers: List[ReleaseIdentifier],
    videos: List[ReleaseVideo],
    genres: List[Genre],
    styles: List[Style],
    tracklist: List[ReleaseTrack],
    extraartists: List[ArtistRelease],
    images: List[ReleaseImage],
    thumb: Option[Uri],
    estimatedWeight: Int,
    blockedFromSale: Boolean
)

final case class DiscogsReleaseId(value: Long) extends AnyVal
final case class ReleaseStatus(value: String)  extends AnyVal

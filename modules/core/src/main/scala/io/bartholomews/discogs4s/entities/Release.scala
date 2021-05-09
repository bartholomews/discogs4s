package io.bartholomews.discogs4s.entities

import java.time.LocalDateTime

import sttp.model.Uri

final case class Release(
    id: Long,
    status: String,
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
    released: String, // not `LocalDate` as it seems to use invalid format (maybe to signal unknown bits, e.g. "1987-07-00")
    notes: String,
    releasedFormatted: String,
    identifiers: List[ReleaseIdentifier],
    videos: List[ReleaseVideo],
    genres: List[String],
    styles: List[Style],
    tracklist: List[ReleaseTrack],
    extraartists: List[ArtistRelease],
    images: List[ReleaseImage],
    thumb: Option[Uri],
    estimatedWeight: Int,
    blockedFromSale: Boolean
)

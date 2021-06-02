package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class MasterRelease(
                                id: MasterId,
                                mainRelease: DiscogsReleaseId,
                                mostRecentRelease: DiscogsReleaseId,
                                uri: Uri,
                                resourceUrl: Uri,
                                versionsUrl: Uri,
                                mainReleaseUrl: Uri,
                                mostRecentReleaseUrl: Uri,
                                numForSale: Int,
                                lowestPrice: BigDecimal,
                                images: List[DiscogsImage],
                                genres: List[Genre],
                                styles: List[Style],
                                year: Int,
                                tracklist: List[ReleaseTrack],
                                artists: List[ArtistRelease],
                                title: String,
                                dataQuality: String,
                                videos: List[ReleaseVideo]
)

final case class MasterId(value: Long) extends AnyVal

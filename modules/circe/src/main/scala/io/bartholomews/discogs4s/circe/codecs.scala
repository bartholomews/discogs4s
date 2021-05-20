package io.bartholomews.discogs4s.circe

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.fsclient.circe.FsClientCirceApi
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredCodec,
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
  deriveUnwrappedCodec
}
import io.circe._
import sttp.model.Uri

object codecs extends DiscogsCirceApi

trait DiscogsCirceApi extends FsClientCirceApi {
  implicit val config: Configuration                 = Configuration.default.withSnakeCaseMemberNames
  implicit val emptyUriDecoder: Decoder[Option[Uri]] = decodeEmptyStringAsOption[Uri]

  implicit val pageUrlsCodec: Codec[PageUrls]     = deriveConfiguredCodec
  implicit val paginationCodec: Codec[Pagination] = deriveConfiguredCodec

  implicit val paginatedReleasesDecoder: Decoder[PaginatedReleases]         = deriveConfiguredDecoder
  implicit val artistSubmissionCodec: Codec[ArtistSubmission]               = deriveConfiguredCodec
  implicit val artistReleaseCodec: Codec[ArtistRelease]                     = deriveConfiguredCodec
  implicit val artistReleaseSubmissionCodec: Codec[ArtistReleaseSubmission] = deriveConfiguredCodec

  implicit val discogsUserIdCodec: Codec[DiscogsUserId]                   = deriveUnwrappedCodec
  implicit val discogsUsernameCodec: Codec[DiscogsUsername]               = deriveUnwrappedCodec
  implicit val discogsUserEmailCodec: Codec[DiscogsUserEmail]             = deriveUnwrappedCodec
  implicit val discogsUserRealNameCodec: Codec[DiscogsUserRealName]       = deriveUnwrappedCodec
  implicit val discogsUserWebsiteCodec: Codec[DiscogsUserWebsite]         = deriveUnwrappedCodec
  implicit val discogsUserLocationCodec: Codec[DiscogsUserLocation]       = deriveUnwrappedCodec
  implicit val discogsUserProfileInfoCodec: Codec[DiscogsUserProfileInfo] = deriveUnwrappedCodec
  implicit val discogsUserResourceCodec: Codec[DiscogsUserResource]       = deriveConfiguredCodec

  implicit val userIdentityDecoder: Decoder[UserIdentity] = Decoder.forProduct4(
    "id",
    "username",
    "resource_url",
    "consumer_name"
  )(UserIdentity.apply)

  implicit val userProfileCodec: Codec[UserProfile] = {
    implicit val dateTimeOffsetDecoder: Decoder[LocalDateTime] =
      localDateTimeDecoder(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    deriveConfiguredCodec
  }

  implicit val ratingAverageCodec: Codec[RatingAverage] = deriveConfiguredCodec

  implicit val marketCodec: Codec[MarketplaceCurrency] = Codec.from(
    Decoder.decodeString.emap(s =>
      MarketplaceCurrency.withNameOption(s).toRight(s"'$s' is not a member of enum MarketplaceCurrency")
    ),
    Encoder.encodeString.contramap(_.entryName)
  )

  implicit val discogsReleaseIdCodec: Codec[DiscogsReleaseId] = deriveUnwrappedCodec

  implicit val releaseTrackCodec: Codec[ReleaseTrack]           = deriveConfiguredCodec
  implicit val releaseVideoCodec: Codec[ReleaseVideo]           = deriveConfiguredCodec
  implicit val releaseImageCodec: Codec[ReleaseImage]           = deriveConfiguredCodec
  implicit val entityResourceCodec: Codec[EntityResource]       = deriveConfiguredCodec
  implicit val formatDescriptionCodec: Codec[FormatDescription] = deriveUnwrappedCodec
  implicit val releaseFormatCodec: Codec[ReleaseFormat] = {
    implicit val decodeFormatDescription: Decoder[List[FormatDescription]] = decodeNullableList[FormatDescription]
    deriveConfiguredCodec
  }
  implicit val styleCodec: Codec[Style] = deriveUnwrappedCodec

  implicit val communityReleaseSubmissionCodec: Codec[CommunityReleaseSubmission] = deriveConfiguredCodec
  implicit val releaseSubmissionCodec: Codec[ReleaseSubmission] = {
    implicit val dateTimeOffsetDecoder: Decoder[LocalDateTime] =
      localDateTimeDecoder(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    implicit val decodeImages: Decoder[List[ReleaseImage]] = decodeNullableList[ReleaseImage]
    implicit val decodeStyles: Decoder[List[Style]]        = decodeNullableList[Style]
    implicit val decodeVideos: Decoder[List[ReleaseVideo]] = decodeNullableList[ReleaseVideo]
    deriveConfiguredCodec
  }
  implicit val userSubmissionCodec: Codec[UserSubmissions]                = deriveConfiguredCodec
  implicit val userSubmissionResponseCodec: Codec[UserSubmissionResponse] = deriveConfiguredCodec

  implicit val releaseIdentifierCodec: Codec[ReleaseIdentifier] = deriveConfiguredCodec
  implicit val releaseCodec: Codec[Release] = {
    implicit val dateTimeOffsetDecoder: Decoder[LocalDateTime] =
      localDateTimeDecoder(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    deriveConfiguredCodec
  }

  implicit val ratingCodec: Codec[Rating] = Codec.from(
    (c: HCursor) =>
      c.as[Int]
        .flatMap({
          case 0     => Right(Rating.NoRating)
          case 1     => Right(Rating.One)
          case 2     => Right(Rating.Two)
          case 3     => Right(Rating.Three)
          case 4     => Right(Rating.Four)
          case 5     => Right(Rating.Five)
          case other => Left(DecodingFailure(s"[$other: unexpected `Rating`]", c.history))
        }),
    Encoder.encodeInt.contramap[Rating](_.value)
  )

  implicit val releaseRatingCodec: Codec[ReleaseRating]             = deriveConfiguredCodec
  implicit val userContributionsCodec: Codec[UserContributions]     = deriveConfiguredCodec
  implicit val updateUserRequestEncoder: Encoder[UpdateUserRequest] = deriveConfiguredEncoder
}

package io.bartholomews.discogs4s.circe

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.fsclient.circe.FsClientCirceApi
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredCodec,
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
  deriveUnwrappedCodec
}
import sttp.model.Uri

object codecs
    extends DiscogsCirceApi

//noinspection DuplicatedCode
trait DiscogsCirceApi extends FsClientCirceApi {
  // TODO: move to fsclient
  def decodeNullableMap[K, V](implicit
      decodeK: KeyDecoder[K],
      decodeV: Decoder[V]
  ): Decoder[Map[K, V]] =
    Decoder.decodeOption[Map[K, V]](Decoder.decodeMap[K, V]).map(_.getOrElse(Map.empty))

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  // TODO: replace in fsclient, or maybe remove it: double check in test with explicit assert on empty Uri
  def decodeEmptyStringAsNone[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = { (c: HCursor) =>
    (c: ACursor) match {
      case cursor: FailedCursor =>
        if (!cursor.incorrectFocus) Right(None) else Left(DecodingFailure("[A]Option[A]", c.history))

      case cursor: HCursor =>
        cursor.focus match {
          case None => Right(None)
          case Some(jValue) =>
            if (jValue.asString.contains("")) Right(None)
            else decoder(c).map(Some(_))
        }
    }
  }

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
    implicit val uriDecoder: Decoder[Option[Uri]] = decodeEmptyStringAsNone[Uri]
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
  implicit val masterIdCodec: Codec[MasterId]                 = deriveUnwrappedCodec
  implicit val releaseStatusCodec: Codec[ReleaseStatus]       = deriveUnwrappedCodec

  implicit val communityReleaseCodec: Codec[CommunityRelease]           = deriveConfiguredCodec
  implicit val communityReleaseStatsCodec: Codec[CommunityReleaseStats] = deriveConfiguredCodec
  implicit val releaseStatsCodec: Codec[ReleaseStats]                   = deriveConfiguredCodec

  implicit val releaseTrackCodec: Codec[ReleaseTrack] = {
    implicit val decodeExtraArtists: Decoder[List[ArtistRelease]] = decodeNullableList[ArtistRelease]
    deriveConfiguredCodec
  }
  implicit val releaseVideoCodec: Codec[ReleaseVideo]           = deriveConfiguredCodec
  implicit val discogsImageCodec: Codec[DiscogsImage]           = deriveConfiguredCodec
  implicit val entityResourceCodec: Codec[EntityResource]       = deriveConfiguredCodec
  implicit val formatDescriptionCodec: Codec[FormatDescription] = deriveUnwrappedCodec
  implicit val releaseFormatCodec: Codec[ReleaseFormat] = {
    implicit val decodeFormatDescription: Decoder[List[FormatDescription]] = decodeNullableList[FormatDescription]
    deriveConfiguredCodec
  }
  implicit val styleCodec: Codec[Style] = deriveUnwrappedCodec
  implicit val genreCodec: Codec[Genre] = deriveUnwrappedCodec

  implicit val communityReleaseSubmissionCodec: Codec[CommunityReleaseSubmission] = deriveConfiguredCodec
  implicit val releaseSubmissionCodec: Codec[ReleaseSubmission] = {
    implicit val dateTimeOffsetDecoder: Decoder[LocalDateTime] =
      localDateTimeDecoder(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    implicit val decodeImages: Decoder[List[DiscogsImage]] = decodeNullableList[DiscogsImage]
    implicit val decodeStyles: Decoder[List[Style]]        = decodeNullableList[Style]
    implicit val decodeVideos: Decoder[List[ReleaseVideo]] = decodeNullableList[ReleaseVideo]
    deriveConfiguredCodec
  }
  implicit val userSubmissionCodec: Codec[UserSubmissions]                = deriveConfiguredCodec
  implicit val userSubmissionResponseCodec: Codec[UserSubmissionResponse] = deriveConfiguredCodec

  implicit val releaseIdentifierCodec: Codec[ReleaseIdentifier] = deriveConfiguredCodec
  implicit val releaseCodec: Codec[Release] = {
    implicit val uriDecoder: Decoder[Option[Uri]] = decodeEmptyStringAsNone[Uri]
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

  implicit val ratingUpdateCodec: Codec[RatingUpdate] = Codec.from(
    (c: HCursor) =>
      c.as[Int]
        .flatMap({
          case 1     => Right(Rating.One)
          case 2     => Right(Rating.Two)
          case 3     => Right(Rating.Three)
          case 4     => Right(Rating.Four)
          case 5     => Right(Rating.Five)
          case other => Left(DecodingFailure(s"[$other: unexpected `Rating`]", c.history))
        }),
    Encoder.encodeInt.contramap[RatingUpdate](_.value)
  )

  implicit val releaseRatingCodec: Codec[ReleaseRating]         = deriveConfiguredCodec
  implicit val userContributionsCodec: Codec[UserContributions] = deriveConfiguredCodec

  implicit val releaseFilterLabel: Codec[ReleaseFilter.Label]                = deriveUnwrappedCodec
  implicit val releaseFilterCountryCodec: Codec[ReleaseFilter.Country]       = deriveUnwrappedCodec
  implicit val releaseFilterFormatCodec: Codec[ReleaseFilter.Format]         = deriveUnwrappedCodec
  implicit val releaseFilterReleasedCodec: Codec[ReleaseFilter.ReleasedYear] = deriveUnwrappedCodec

  implicit val availableFiltersCodec: Codec[AvailableFilters] = {
    implicit def decodeFilter: Decoder[Map[String, Int]] = decodeNullableMap
    deriveConfiguredCodec
  }

  implicit val appliedFiltersCodec: Codec[AppliedFilters] = {
    implicit def decodeFilter[R <: ReleaseFilter](implicit d: Decoder[R]): Decoder[List[R]] = decodeNullableList[R]
    deriveConfiguredCodec
  }

  implicit val aliasCodec: Codec[Alias]       = deriveConfiguredCodec
  implicit val groupCodec: Codec[Group]       = deriveConfiguredCodec
  implicit val artistIdCodec: Codec[ArtistId] = deriveUnwrappedCodec
  implicit val artistCodec: Codec[Artist]     = deriveConfiguredCodec

  implicit val filtersInfoCodec: Codec[FiltersInfo]                     = deriveConfiguredCodec
  implicit val filterFacetCodec: Codec[FilterFacet]                     = deriveConfiguredCodec
  implicit val filterFacetValueCodec: Codec[FilterFacetValue]           = deriveConfiguredCodec
  implicit val releaseVersionCodec: Codec[ReleaseVersion]               = deriveConfiguredCodec
  implicit val masterReleaseCodec: Codec[MasterRelease]                 = deriveConfiguredCodec
  implicit val masterReleaseVersionsCodec: Codec[MasterReleaseVersions] = deriveConfiguredCodec

  implicit val releaseRatingUpdateRequestEncoder: Encoder[ReleaseRatingUpdateRequest] = deriveConfiguredEncoder
  implicit val updateUserRequestEncoder: Encoder[UpdateUserRequest]                   = deriveConfiguredEncoder

  implicit val labelIdCodec: Codec[Label.Id]     = deriveUnwrappedCodec
  implicit val labelNameCodec: Codec[Label.Name] = deriveUnwrappedCodec
  implicit val labelCodec: Codec[Label]          = deriveConfiguredCodec
}

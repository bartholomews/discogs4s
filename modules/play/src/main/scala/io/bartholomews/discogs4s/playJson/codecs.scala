package io.bartholomews.discogs4s.playJson

import ai.x.play.json.{CamelToSnakeNameEncoder, Jsonx, NameEncoder}
import enumeratum.EnumFormats
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.fsclient.play.FsClientPlayApi
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

import scala.util.Try

object codecs extends DiscogsPlayJsonApi

trait DiscogsPlayJsonApi extends FsClientPlayApi {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val encoder: NameEncoder           = CamelToSnakeNameEncoder()

  implicit val pageUrlsCodec: Format[PageUrls]     = Json.format
  implicit val paginationCodec: Format[Pagination] = Json.format

  implicit val discogsUserIdCodec: Format[DiscogsUserId]                   = Json.valueFormat
  implicit val discogsUsernameCodec: Format[DiscogsUsername]               = Json.valueFormat
  implicit val discogsUserEmailCodec: Format[DiscogsUserEmail]             = Json.valueFormat
  implicit val discogsUserRealNameCodec: Format[DiscogsUserRealName]       = Json.valueFormat
  implicit val discogsUserWebsiteCodec: Format[DiscogsUserWebsite]         = Json.valueFormat
  implicit val discogsUserLocationCodec: Format[DiscogsUserLocation]       = Json.valueFormat
  implicit val discogsUserProfileInfoCodec: Format[DiscogsUserProfileInfo] = Json.valueFormat
  implicit val discogsUserResourceCodec: Format[DiscogsUserResource]       = Json.format

  implicit val ratingCodec: Format[Rating] = Json.format

  // FIXME: This doesn't seem to work?
  def decodeNullableList[A](implicit rds: Reads[A]): Reads[List[A]] =
    (json: JsValue) =>
      json
        .validateOpt[List[A]](Reads.list(rds))
        .map(_.getOrElse(Nil))

  def decodeOptionAsEmptyString[A](implicit read: Reads[A]): Reads[Option[A]] = { (json: JsValue) =>
    json
      .validateOpt[String]
      .flatMap({
        case None      => JsSuccess(None)
        case Some(str) => if (str.isEmpty) JsSuccess(None) else json.validateOpt[A]
      })
  }

  def jsResultEmptyStringAsNone[A](jsLookupResult: JsLookupResult)(implicit rds: Reads[A]): JsResult[Option[A]] =
    jsLookupResult
      .validate[String]
      .flatMap { str =>
        if (str.isEmpty) JsSuccess(None) else jsLookupResult.validate[A].map(Some(_))
      }

  implicit val userIdentityReads: Reads[UserIdentity] = Json.reads[UserIdentity]
  implicit val userProfileReads: Reads[UserProfile]   = UserProfilePlayJson.reads

  implicit val discogsReleaseIdCodec: Format[DiscogsReleaseId] = Json.valueFormat

  implicit val releaseTrackCodec: Format[ReleaseTrack]           = Json.format
  implicit val releaseVideoCodec: Format[ReleaseVideo]           = Json.format
  implicit val releaseImageCodec: Format[ReleaseImage]           = Json.format
  implicit val entityResourceCodec: Format[EntityResource]       = Json.format
  implicit val formatDescriptionCodec: Format[FormatDescription] = Json.valueFormat
  implicit val releaseFormatCodec: Format[ReleaseFormat] = {
    val writes: Writes[ReleaseFormat] = Json.writes
    val reads: Reads[ReleaseFormat] = { (json: JsValue) =>
      for {
        descriptions <- (json \ "descriptions").validateOpt[List[FormatDescription]].map(_.getOrElse(Nil))
        name         <- (json \ "name").validate[String]
        qty          <- (json \ "qty").validate[String].flatMap(str => JsResult.fromTry(Try(str.toInt)))
      } yield ReleaseFormat(name, qty, descriptions)
    }
    Format(reads, writes)
  }

  implicit val styleCodec: Format[Style] = Json.valueFormat

  implicit val artistReleaseCodec: Format[ArtistRelease]                           = Json.format
  implicit val artistSubmissionCodec: Format[ArtistSubmission]                     = Json.format
  implicit val artistReleaseSubmissionCodec: Format[ArtistReleaseSubmission]       = Json.format
  implicit val communityReleaseSubmissionCodec: Format[CommunityReleaseSubmission] = Json.format
  implicit val releaseSubmissionCodec: Format[ReleaseSubmission]                   = ReleaseSubmissionPlayJson.codec
  implicit val userSubmissionCodec: Format[UserSubmissions]                        = Json.format
  implicit val userSubmissionResponseCodec: Format[UserSubmissionResponse]         = Json.format

  implicit val userContributionsCodec: Format[UserContributions] = Json.format

  implicit val releaseIdentifierCodec: Format[ReleaseIdentifier] = Json.format
  implicit val releaseCodec: Reads[Release]                      = Jsonx.formatCaseClass[Release]

  implicit val paginatedReleasesReads: Reads[PaginatedReleases] = Json.reads[PaginatedReleases]

  implicit val marketplaceCurrencyCodec: Format[MarketplaceCurrency] = EnumFormats.formats(MarketplaceCurrency)
  implicit val updateUserRequestWrites: Writes[UpdateUserRequest]    = Json.writes[UpdateUserRequest]
}

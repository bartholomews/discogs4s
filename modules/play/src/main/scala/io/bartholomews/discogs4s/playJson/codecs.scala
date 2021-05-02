package io.bartholomews.discogs4s.playJson

import ai.x.play.json.{CamelToSnakeNameEncoder, Jsonx, NameEncoder}
import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.play.FsClientPlayApi
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import sttp.model.Uri

object codecs extends DiscogsPlayJsonApi

trait DiscogsPlayJsonApi extends FsClientPlayApi {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val encoder: NameEncoder = CamelToSnakeNameEncoder()

  implicit val usernameFormat: Format[Username] = Json.valueFormat
  implicit val userEmailFormat: Format[UserEmail] = Json.valueFormat
  implicit val userRealNameFormat: Format[UserRealName] = Json.valueFormat
  implicit val userWebsiteFormat: Format[UserWebsite] = Json.valueFormat
  implicit val userLocationFormat: Format[UserLocation] = Json.valueFormat
  implicit val userProfileInfoFormat: Format[UserProfileInfo] = Json.valueFormat

  def decodeOptionAsEmptyString[A](implicit read: Reads[A]): Reads[Option[A]] = { (json: JsValue) =>
    json
      .validateOpt[String]
      .flatMap({
        case None      => JsSuccess(None)
        case Some(str) => if (str.isEmpty) JsSuccess(None) else json.validateOpt[A]
      })
  }

  implicit val pageUrlsReads: Reads[PageUrls] = Json.reads[PageUrls]
  implicit val paginationReads: Reads[Pagination] = Json.reads[Pagination]
  implicit val releaseReads: Reads[Release] = Json.reads[Release]
  implicit val simpleUserReads: Reads[SimpleUser] = {
    // TODO: Double check that this is picked up
    implicit val emptyUriReads: Reads[Option[Uri]] = decodeOptionAsEmptyString[Uri]
    Jsonx.formatCaseClass[SimpleUser]
  }

  implicit val userIdentityReads: Reads[UserIdentity] = Json.reads[UserIdentity]
  implicit val authenticatedUserReads: Reads[AuthenticatedUser] = Jsonx.formatCaseClass[AuthenticatedUser]
  implicit val paginatedReleasesReads: Reads[PaginatedReleases] = Json.reads[PaginatedReleases]
}

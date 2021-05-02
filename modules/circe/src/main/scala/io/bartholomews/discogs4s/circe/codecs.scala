package io.bartholomews.discogs4s.circe

import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.circe.FsClientCirceApi
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.{semiauto, Configuration}
import io.circe.{Decoder, HCursor}
import sttp.model.Uri

object codecs extends DiscogsCirceApi

trait DiscogsCirceApi extends FsClientCirceApi {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  def decodeOptionAsEmptyString[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = { (c: HCursor) =>
    c.focus match {
      case None => Right(None)
      case Some(jValue) =>
        if (jValue.asString.contains("")) Right(None)
        else decoder(c).map(Some(_))
    }
  }

  implicit val pageUrlsDecoder: Decoder[PageUrls] = deriveConfiguredDecoder[PageUrls]
  implicit val paginationDecoder: Decoder[Pagination] = deriveConfiguredDecoder[Pagination]
  implicit val paginatedReleasesDecoder: Decoder[PaginatedReleases] = deriveConfiguredDecoder[PaginatedReleases]
  implicit val releaseDecoder: Decoder[Release] = {
    deriveConfiguredDecoder[Release]
  }
  implicit val authenticatedUserDecoder: Decoder[AuthenticatedUser] = deriveConfiguredDecoder[AuthenticatedUser]
  implicit val simpleUserDecoder: Decoder[SimpleUser] = {
    implicit val emptyUriDecoder: Decoder[Option[Uri]] = decodeOptionAsEmptyString[Uri]
    deriveConfiguredDecoder[SimpleUser]
  }

  implicit val userIdentityDecoder: Decoder[UserIdentity] = Decoder.forProduct4(
    "id",
    "username",
    "resource_url",
    "consumer_name"
  )(UserIdentity.apply)

  implicit val usernameDecoder: Decoder[Username] = semiauto.deriveUnwrappedDecoder
  implicit val userEmailDecoder: Decoder[UserEmail] = semiauto.deriveUnwrappedDecoder
  implicit val userRealNameDecoder: Decoder[UserRealName] = semiauto.deriveUnwrappedDecoder
  implicit val userWebsiteDecoder: Decoder[UserWebsite] = semiauto.deriveUnwrappedDecoder
  implicit val userLocationDecoder: Decoder[UserLocation] = semiauto.deriveUnwrappedDecoder
  implicit val userProfileInfoDecoder: Decoder[UserProfileInfo] = semiauto.deriveUnwrappedDecoder
}

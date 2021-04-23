package io.bartholomews.discogs4s.circe

import io.bartholomews.discogs4s.entities._
import io.bartholomews.fsclient.circe.FsClientCirceApi
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.{Configuration, semiauto}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Codec, Decoder, Encoder, HCursor}
import sttp.model.Uri

object codecs extends FsClientCirceApi {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  // FIXME: Should be able to remove this and load from fsclient
  implicit val uriCodec: Codec[Uri] = Codec.from(
    Decoder.decodeString.emap(Uri.parse),
    Encoder.encodeString.contramap(_.toString)
  )

  def decodeOptionAsEmptyString[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = { (c: HCursor) =>
    c.focus match {
      case None => Right(None)
      case Some(jValue) =>
        if (jValue.asString.contains("")) Right(None)
        else decoder(c).map(Some(_))
    }
  }

  implicit val pageUrlsDecoder: Decoder[PageUrls] = deriveDecoder[PageUrls]
  implicit val paginationDecoder: Decoder[Pagination] = deriveDecoder[Pagination]
  implicit val paginatedReleasesDecoder: Decoder[PaginatedReleases] = deriveDecoder[PaginatedReleases]
  implicit val releaseDecoder: Decoder[Release] = deriveDecoder[Release]
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

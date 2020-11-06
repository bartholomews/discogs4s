package io.bartholomews.discogs4s.entities

import io.circe.Decoder
import org.http4s.Uri

case class UserIdentity(id: Long, username: String, resourceUrl: Uri, consumerName: String) extends DiscogsEntity

object UserIdentity {
  implicit val decoder: Decoder[UserIdentity] = Decoder.forProduct4(
    "id",
    "username",
    "resource_url",
    "consumer_name"
  )(UserIdentity.apply)
}

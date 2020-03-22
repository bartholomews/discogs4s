package io.bartholomews.discogs4s.entities

import fsclient.codecs.FsJsonResponsePipe
import io.circe.Decoder
import org.http4s.Uri
import org.http4s.circe.decodeUri

case class UserIdentity(id: Long, username: String, resourceUrl: Uri, consumerName: String) extends DiscogsEntity

object UserIdentity extends FsJsonResponsePipe[UserIdentity] {
  // FIXME: `decodeUri` seems to accept any String to create a valid Uri, double check `Uri.fromString`
  implicit val decoder: Decoder[UserIdentity] = Decoder.forProduct4(
    "id",
    "username",
    "resource_url",
    "consumer_name"
  )(UserIdentity.apply)
}

package io.bartholomews.discogs4s.playJson

import io.bartholomews.discogs4s.entities._
import play.api.libs.json._

private[playJson] object ReleaseTrackPlayJson {

  import codecs._

  private val writes: Writes[ReleaseTrack] = Json.writes
  private val reads: Reads[ReleaseTrack] = { (json: JsValue) =>
    for {
      position     <- (json \ "position").validate[String]
      type_        <- (json \ "type_").validate[String]
      title        <- (json \ "title").validate[String]
      duration     <- (json \ "duration").validate[String]
      extraartists <- (json \ "extraartists").validateOpt[List[ArtistRelease]].map(_.getOrElse(Nil))
    } yield ReleaseTrack(
      position,
      `type_`,
      title,
      duration,
      extraartists
    )
  }

  val codec: Format[ReleaseTrack] = Format.apply(reads, writes)
}

package io.bartholomews.discogs4s.playJson

import java.time.LocalDateTime

import ai.x.play.json.Jsonx
import io.bartholomews.discogs4s.entities._
import io.bartholomews.discogs4s.playJson.codecs.decodeNullableList
import play.api.libs.json._
import sttp.model.Uri

object ReleaseSubmissionPlayJson {

  import codecs._

  private val writes: Writes[ReleaseSubmission] = Jsonx.formatCaseClass[ReleaseSubmission]

  private val reads: Reads[ReleaseSubmission] = { (json: JsValue) =>
    for {
      artists           <- (json \ "artists").validate[List[ArtistReleaseSubmission]]
      community         <- (json \ "community").validate[CommunityReleaseSubmission]
      country           <- (json \ "country").validateOpt[String]
      dataQuality       <- (json \ "data_quality").validate[String]
      dateAdded         <- (json \ "date_added").validate[LocalDateTime]
      dateChanged       <- (json \ "date_changed").validate[LocalDateTime]
      estimatedWeight   <- (json \ "estimated_weight").validateOpt[Int]
      formatQuantity    <- (json \ "format_quantity").validate[Int]
      formats           <- (json \ "formats").validate[List[ReleaseFormat]]
      genres            <- (json \ "genres").validate[List[String]]
      id                <- (json \ "id").validate[Long]
      images            <- (json \ "images").validateOpt[List[ReleaseImage]].map(_.getOrElse(Nil))
      labels            <- (json \ "labels").validate[List[ReleaseLabel]]
      masterId          <- (json \ "master_id").validateOpt[Long]
      masterUrl         <- (json \ "master_url").validateOpt[String]
      notes             <- (json \ "notes").validateOpt[String]
      released          <- (json \ "released").validateOpt[String]
      releasedFormatted <- (json \ "released_formatted").validateOpt[String]
      resourceUrl       <- (json \ "resource_url").validate[Uri]
      status            <- (json \ "status").validate[String]
      styles            <- (json \ "styles").validateOpt[List[Style]].map(_.getOrElse(Nil))
      thumb             <- (json \ "thumb").validate[Uri]
      title             <- (json \ "title").validate[String]
      uri               <- (json \ "uri").validate[Uri]
      videos            <- (json \ "videos").validateOpt[List[ReleaseVideo]].map(_.getOrElse(Nil))
      year              <- (json \ "year").validate[Int]
    } yield ReleaseSubmission(
      artists,
      community,
      country,
      dataQuality,
      dateAdded,
      dateChanged,
      estimatedWeight,
      formatQuantity,
      formats,
      genres,
      id,
      images,
      labels,
      masterId,
      masterUrl,
      notes,
      released,
      releasedFormatted,
      resourceUrl,
      status,
      styles,
      thumb,
      title,
      uri,
      videos,
      year
    )
  }

  val codec: Format[ReleaseSubmission] = Format.apply(reads, writes)
}

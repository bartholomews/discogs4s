package io.bartholomews.discogs4s.entities

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class Pagination(page: Int, pages: Int, items: Long, per_page: Int, urls: PageUrls)

object Pagination {
  implicit val decoder: Decoder[Pagination] = deriveDecoder[Pagination]
}

case class PageUrls(first: Option[String], prev: Option[String], next: String, last: String)
object PageUrls {
  implicit val decoder: Decoder[PageUrls] = deriveDecoder[PageUrls]
}

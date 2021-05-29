package io.bartholomews.discogs4s.entities

final case class Pagination(page: Int, pages: Int, items: Long, perPage: Int, urls: Option[PageUrls])
final case class PageUrls(first: Option[String], prev: Option[String], next: Option[String], last: Option[String])

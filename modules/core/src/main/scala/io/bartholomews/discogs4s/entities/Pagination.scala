package io.bartholomews.discogs4s.entities

case class Pagination(page: Int, pages: Int, items: Long, per_page: Int, urls: PageUrls)

case class PageUrls(first: Option[String], prev: Option[String], next: String, last: String)

package io.bartholomews.discogs4s.entities

final case class ReleaseFormat(name: String, qty: Int, descriptions: List[FormatDescription])

final case class FormatDescription(value: String) extends AnyVal

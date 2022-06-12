package io.bartholomews.discogs4s.entities

import io.bartholomews.discogs4s.entities.Label._

final case class Label(id: Id, name: Name)
object Label {
  final case class Id(value: Long)     extends AnyVal
  final case class Name(value: String) extends AnyVal
}

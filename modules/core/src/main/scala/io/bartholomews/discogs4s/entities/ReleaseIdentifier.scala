package io.bartholomews.discogs4s.entities

/*
types:
--------
Barcode
Label Code
Matrix / Runout
Price Code
???
 */
final case class ReleaseIdentifier(
    `type`: String,
    value: String,
    description: Option[String]
)

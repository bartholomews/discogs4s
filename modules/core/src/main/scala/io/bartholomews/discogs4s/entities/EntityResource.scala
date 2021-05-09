package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class EntityResource(
    name: String,
    catno: String,
    entityType: String,
    entityTypeName: String,
    id: Long,
    resourceUrl: Uri
)

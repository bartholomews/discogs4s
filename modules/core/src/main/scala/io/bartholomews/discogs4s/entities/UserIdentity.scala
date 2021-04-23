package io.bartholomews.discogs4s.entities

import sttp.model.Uri

case class UserIdentity(id: Long, username: String, resourceUrl: Uri, consumerName: String) extends DiscogsEntity

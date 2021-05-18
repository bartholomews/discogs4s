package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class UserIdentity(id: Long, username: DiscogsUsername, resourceUrl: Uri, consumerName: String)
    extends DiscogsEntity

package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class DiscogsImage(`type`: String, width: Int, height: Int, uri: Uri, uri150: Uri, resourceUrl: Uri)

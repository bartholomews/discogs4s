package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class ReleaseImage(height: Int, resourceUrl: Uri, `type`: String, uri: Uri, uri150: Uri, width: Int)

package io.bartholomews.discogs4s.entities

import sttp.model.Uri

final case class ReleaseImage(`type`: String, width: Int, height: Int, uri: Uri, uri150: Uri, resourceUrl: Uri)

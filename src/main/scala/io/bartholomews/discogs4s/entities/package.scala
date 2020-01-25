package io.bartholomews.discogs4s

import io.circe.generic.extras.Configuration

package object entities {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
}

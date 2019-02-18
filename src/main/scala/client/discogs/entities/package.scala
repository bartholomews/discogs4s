package client.discogs

import io.circe.generic.extras.Configuration

package object entities {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
}

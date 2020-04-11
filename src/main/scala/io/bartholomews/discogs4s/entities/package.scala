package io.bartholomews.discogs4s

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import org.http4s.Uri

package object entities {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  // FIXME: `decodeUri` seems to accept any String to create a valid Uri, double check `Uri.fromString`
  //  double check and in case use `decodeUri` with unsafeFromString + `catchOnly` or something similar
  implicit val uriDecoder: Decoder[Uri] = org.http4s.circe.decodeUri
    // Decoder.decodeString.map(Uri.unsafeFromString)
}

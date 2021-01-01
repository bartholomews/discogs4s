package io.bartholomews.discogs4s

import io.circe.generic.extras.Configuration
import io.circe.{Codec, Decoder, Encoder, HCursor}
import sttp.model.Uri

package object entities {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  // FIXME: Should be able to remove this and load from fsclient
  implicit val uriCodec: Codec[Uri] = Codec.from(
    Decoder.decodeString.emap(Uri.parse),
    Encoder.encodeString.contramap(_.toString)
  )

  def decodeOptionAsEmptyString[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = { (c: HCursor) =>
    c.focus match {
      case None => Right(None)
      case Some(jValue) =>
        if (jValue.asString.contains("")) Right(None)
        else decoder(c).map(Some(_))
    }
  }
}

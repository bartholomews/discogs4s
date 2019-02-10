import io.circe._
import org.http4s.Uri

package object entities {

  //  implicit val decodeUri: Decoder[Uri] = (c: HCursor) =>
  //    c.value.as[String]
  //      .flatMap(str => Uri.fromString(str).left.map(parseFailure =>
  //        DecodingFailure(parseFailure.message, List(CursorOp.DownField("WTF")))
  //      ))

//  implicit val decodeUri: Decoder[Uri] = (c: HCursor) => for {
//    foo <- c.downField("resource_url").as[String]
//    bar <- Uri.fromString(foo).left.map(parseFailure => {
//      println(parseFailure.message)
//      println(parseFailure.details)
//      DecodingFailure(parseFailure.message, List.empty)
//    })
//  } yield bar

  //    Decoder.apply(e => {
  //    val s: Result[String] = e.downField(field).as[String]
  //    s.map(str => Uri.fromString(str).left.map(parseFailure =>
  //      DecodingFailure(parseFailure.message, List(CursorOp.DownField(field)))))
  //        .joinRight
  //  })
}

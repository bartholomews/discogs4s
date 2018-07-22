package entities

import io.circe.DecodingFailure
import org.http4s.Status

case class ResponseError(throwable: Throwable, status: Status) extends Exception {
  override val getMessage: String = {
//    if(throwable.isInstanceOf[DecodingFailure]) {
//    }
    throwable.getMessage
  }
}

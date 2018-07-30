package client

import cats.effect.Effect
import fs2.Stream
import org.http4s.Request
import org.http4s.client.oauth1.Consumer
import utils.Logger

trait PlainTextRequest extends RequestF[String] with Logger {
  def plainText[F[_] : Effect](request: Request[F])
                              (implicit consumer: Consumer): Stream[F, Either[Throwable, String]] = {
    withLogger {
      fetch(request)(res => Stream.eval(res.as[String]).attempt)
    }
  }
}

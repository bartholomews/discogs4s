package client.effect4s

import cats.effect.Effect
import client.effect4s.entities.OAuthAccessToken
import org.http4s.Request

trait OAuthSignature {

  import org.http4s.client.oauth1._

  private[effect4s] def sign[F[_] : Effect](consumer: Consumer, accessToken: Option[OAuthAccessToken] = None)
                                           (req: Request[F]): F[Request[F]] = {
    signRequest(
      req,
      consumer,
      callback = None,
      verifier = accessToken.flatMap(_.verifier),
      accessToken.map(_.token)
    )
  }

}

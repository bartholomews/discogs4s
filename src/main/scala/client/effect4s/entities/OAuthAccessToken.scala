package client.effect4s.entities

import org.http4s.client.oauth1.Token

trait OAuthAccessToken {
  val token: Token
  val verifier: Option[String] = None
}

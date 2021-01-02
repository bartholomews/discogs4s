package io.bartholomews.discogs4s.samples

object PersonalTokenReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.UserIdentity
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.OAuthSigner
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  private val discogs = DiscogsClient.personalFromConfig

  implicit val personalToken: OAuthSigner = discogs.client.signer

  // You can make authenticated (for your user only) calls with the implicit signer
  val response: F[SttpResponse[circe.Error, UserIdentity]] = discogs.users.me
}

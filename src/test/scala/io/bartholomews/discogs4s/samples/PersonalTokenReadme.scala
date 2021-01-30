package io.bartholomews.discogs4s.samples

object PersonalTokenReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.UserIdentity
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.OAuthSigner
  import io.circe
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  private val discogs = DiscogsClient.personalFromConfig(backend)
  implicit val personalToken: OAuthSigner = discogs.client.signer

  // You can make authenticated (for your user only) calls with the implicit signer
  val response: F[SttpResponse[circe.Error, UserIdentity]] = discogs.users.me
}

package io.bartholomews.discogs4s.samples

object SimpleClientReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  // you could also pass the credentials directly in `DiscogsClient.clientCredentials`
  private val client = DiscogsClient.clientCredentialsFromConfig

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}

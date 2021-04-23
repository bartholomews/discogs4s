package samples

object ClientCredentialsReadme extends App {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  //
  import io.bartholomews.discogs4s.circe.codecs._

  // you could also pass the credentials directly in `DiscogsClient.clientCredentials`
  private val client = DiscogsClient.clientCredentialsFromConfig(backend)

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}

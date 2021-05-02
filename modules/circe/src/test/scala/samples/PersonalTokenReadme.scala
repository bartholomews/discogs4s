package samples

object PersonalTokenReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.UserIdentity
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  import io.bartholomews.discogs4s.circe.codecs._

  private val discogs = DiscogsClient.personal.unsafeFromConfig(backend)

  // You can make authenticated (for your user only) calls with the implicit signer
  val response: F[SttpResponse[circe.Error, UserIdentity]] = discogs.users.me
}

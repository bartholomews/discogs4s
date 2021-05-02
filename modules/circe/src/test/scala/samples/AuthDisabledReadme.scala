package samples

object AuthDisabledReadme extends App {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  // import the response handler and token response decoder
  // (here using the circe module, you can also use the play framework or provide your own if using core module)
  import io.bartholomews.discogs4s.circe.codecs._

  // create a basic client ready to make (unsigned) requests:
  private val client = DiscogsClient.authDisabled(
    UserAgent(appName = "my-app", appVersion = None, appUrl = None)
  )(backend)

  // run a request with your client
  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}

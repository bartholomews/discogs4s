package samples

object AuthDisabledReadme extends App {
  import io.bartholomews.discogs4s.entities.{UserProfile, Username}
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsSimpleClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.AuthDisabled
  import io.circe
  import pureconfig.ConfigReader.Result
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  // import the response handler and token response decoder
  // (here using the circe module, you can also use the play framework or provide your own if using core module)
  import io.bartholomews.discogs4s.circe.codecs._

  /*
  // In `application.conf`:

  user-agent {
      app-name = "<YOUR_APP_NAME>"
      app-version = "<OPTIONAL_APP_VERSION>"
      app-url = "<OPTIONAL_APP_URL>"
  }
   */
  // create a basic client ready to make (unsigned) requests:
  private val client = DiscogsClient.authDisabled.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsSimpleClient[F, AuthDisabled.type]] =
    DiscogsClient.authDisabled.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `Consumer` directly
  private val explicitClient = DiscogsClient.authDisabled.apply(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>"))
  )(backend)

  // run a request with your client
  val response: F[SttpResponse[circe.Error, UserProfile]] =
    client.users.getUserProfile(Username("_.bartholomews"))
}

package samples

object PersonalTokenReadme extends App {
  import io.bartholomews.discogs4s.entities.UserIdentity
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsPersonalClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.OAuthSigner
  import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.AccessToken
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

  discogs {
    access-token: "<YOUR_PERSONAL_ACCESS_TOKEN>"
  }
   */
  private val client = DiscogsClient.personal.unsafeFromConfig(backend)
  // or you can create a safe client from config (i.e. `Either[ConfigReaderFailures, DiscogsPersonalClient[F, OAuthSigner]]`
  private val safeClient: Result[DiscogsPersonalClient[F, OAuthSigner]] =
    DiscogsClient.personal.fromConfig(backend)
  // you can also client providing `UserAgent` and `AccessToken` directly
  private val explicitClient = DiscogsClient.personal(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    AccessToken(value = "<YOUR_PERSONAL_ACCESS_TOKEN>")
  )(backend)

  // You can make authenticated (for your user only) calls
  val response: F[SttpResponse[circe.Error, UserIdentity]] = client.users.me
}
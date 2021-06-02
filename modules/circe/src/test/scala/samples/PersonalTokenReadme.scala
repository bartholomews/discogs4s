package samples

object PersonalTokenReadme extends App {
  import _root_.io.bartholomews.discogs4s.entities.{DiscogsUsername, UserIdentity}
  import _root_.io.bartholomews.discogs4s.{DiscogsClient, DiscogsPersonalClient}
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
    username: "<YOUR_DISCOGS_USERNAME>"
  }
   */
  private val client = DiscogsClient.personal.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsPersonalClient[F, OAuthSigner]] =
    DiscogsClient.personal.fromConfig(backend)
  // you can also create a client providing `UserAgent`, `AccessToken` and `DiscogsUsername` directly
  // (make sure you enter the correct username string, otherwise auth calls will be rejected)
  private val explicitClient = DiscogsClient.personal(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    AccessToken(value = "<YOUR_PERSONAL_ACCESS_TOKEN>"),
    DiscogsUsername(value = "<YOUR_DISCOGS_USERNAME>")
  )(backend)

  // You can make authenticated (for your user only) calls
  val response: F[SttpResponse[circe.Error, UserIdentity]] = client.users.me
}

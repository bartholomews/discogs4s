package samples

object ClientCredentialsReadme extends App {
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsSimpleClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.SignerV1
  import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.Consumer
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
    consumer {
      key: "<YOUR_CONSUMER_KEY>",
      secret: "<YOUR_CONSUMER_SECRET>"
    }
  }
  */
  private val client = DiscogsClient.clientCredentials.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsSimpleClient[F, SignerV1]] = DiscogsClient.clientCredentials.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `Consumer` directly
  private val explicitClient = DiscogsClient.clientCredentials.apply(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    Consumer(key = "<YOUR_CONSUMER_KEY>", secret = "<YOUR_CONSUMER_SECRET>")
  )(backend)

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}

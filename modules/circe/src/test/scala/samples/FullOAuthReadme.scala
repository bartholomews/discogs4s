package samples

object FullOAuthReadme {
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsOAuthClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.Consumer
  import io.bartholomews.fsclient.core.oauth.{RedirectUri, TemporaryCredentialsRequest}
  import pureconfig.ConfigReader.Result
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext}
  import sttp.model.Uri

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
  private val client = DiscogsClient.oAuth.unsafeFromConfig(backend)
  // or you can create a safe client from config (i.e. `Either[ConfigReaderFailures, DiscogsOAuthClient[F]]`
  private val safeClient: Result[DiscogsOAuthClient[F]] =
    DiscogsClient.oAuth.fromConfig(backend)
  // you can also client providing `UserAgent` and `Consumer` directly
  private val explicitClient = DiscogsClient.oAuth.apply(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    Consumer(key = "<YOUR_CONSUMER_KEY>", secret = "<YOUR_CONSUMER_SECRET>")
  )(backend)

  // the uri to be redirected after the user will grant permissions for your app
  private val redirectUri = RedirectUri(uri"http://localhost:9000/discogs/callback")

  // prepare your credentials request
  val temporaryCredentialsRequest: TemporaryCredentialsRequest =
    client.temporaryCredentialsRequest(redirectUri)

  for {
    temporaryCredentials <- client.auth.getRequestToken(temporaryCredentialsRequest).body

    // After you get the temporary credentials, you server should redirect the user
    // to `temporaryCredentials.resourceOwnerAuthorizationRequest`
    // which is the discogs token uri where the user will grant permissions to your app
    sendTheUserTo: Uri = temporaryCredentials.resourceOwnerAuthorizationRequest

    /*
      After the user grants/rejects permissions to your app at `sendTheUserTo` uri,
      they will be redirected to `redirectUri`: the url will have
      query parameters with the token key and verifier (if permissions have been granted)
     */
    resourceOwnerAuthorizationUriResponse: Uri = redirectUri.value.withParams(
      Map("oauth_token" -> "AAA", "oauth_verifier" -> "ZZZ")
    )

    /*
      finally get the access token credentials; this call will give an appropriate error message
      if the user has rejected permissions; the access token can be serialized / stored somewhere
      (it doesn't expire).
      By default the OAuth signature is using SHA1, you can override and use PLAINTEXT instead
      (for more info see https://tools.ietf.org/html/rfc5849#section-3.4).
     */
    accessToken <- client.auth.fromUri(resourceOwnerAuthorizationUriResponse, temporaryCredentials).body

  } yield {
    // you need to provide an accessToken to make user-authenticated calls
    client.users.me(accessToken).body match {
      case Left(error) => println(error.getMessage)
      case Right(user) => println(user.username)
    }
  }
}

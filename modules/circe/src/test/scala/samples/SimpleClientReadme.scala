package samples

object SimpleClientReadme extends App {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  //
  import io.bartholomews.discogs4s.circe.codecs._

  /*
    create a basic client ready to make (unsigned) requests:
    you can also use `basicFromConfig` but need to have user-agent in config
   */
  private val client = DiscogsClient.basic(
    UserAgent(appName = "my-app", appVersion = None, appUrl = None)
  )(backend)

  // run a request with your client
  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}

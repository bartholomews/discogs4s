package io.bartholomews.discogs4s.samples

object ClientCredentialsReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  // create a basic client ready to make (unsigned) requests
  private val client = DiscogsClient.clientCredentialsFromConfig

  // run a request with your client
  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))

}

package io.bartholomews.discogs4s.samples

object PersonalTokenReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  private val client = DiscogsClient.personalFromConfig

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
}


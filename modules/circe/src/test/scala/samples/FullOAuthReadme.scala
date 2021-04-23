package samples

import io.bartholomews.fsclient.core.oauth.RedirectUri

object FullOAuthReadme {
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, SignerV1, TemporaryCredentialsRequest}
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext}
  import sttp.model.Uri

  type F[X] = Identity[X]
  val backend: SttpBackend[F, Any] = HttpURLConnectionBackend()

  //
  import io.bartholomews.discogs4s.circe.codecs._

  val discogsClient: DiscogsClient[F, SignerV1] =
    DiscogsClient.clientCredentialsFromConfig(backend)

  // the uri to be redirected after the user will grant permissions for your app
  private val redirectUri = RedirectUri(uri"http://localhost:9000/discogs/callback")

  val temporaryCredentialsRequest: TemporaryCredentialsRequest =
    discogsClient.temporaryCredentialsRequest(redirectUri)

  for {
    temporaryCredentials <- discogsClient.auth.getRequestToken(temporaryCredentialsRequest).body

    // Send the uri to discogs token uri to give permissions to your app
    sendTheUserTo: Uri = temporaryCredentials.resourceOwnerAuthorizationRequest

    /*
      After the user accept/reject permissions for your app at `sendTheUserTo` uri,
      they will be redirected to `redirectUri`: the url will have
      query parameters with the token key and verifier;
      it doesn't seem to have the token secret,
      that's why you need to keep the temporary credentials in the previous step
     */
    resourceOwnerAuthorizationUriResponse: Uri = redirectUri.value.withParams(
      Map("oauth_token" -> "AAA", "oauth_verifier" -> "ZZZ")
    )

    /*
      finally get the access token credentials:
      you could serialize it in the client session cookies or store it somewhere
      (it doesn't expire).
      By default the OAuth signature is using SHA1, you can override and use PLAINTEXT instead
      (for more info see https://tools.ietf.org/html/rfc5849#section-3.4).
     */
    accessToken <- discogsClient.auth.fromUri(resourceOwnerAuthorizationUriResponse, temporaryCredentials).body

  } yield {
    // you need to provide an accessToken to make user-authenticated calls
    discogsClient.users.me(accessToken).body match {
      case Left(error) => println(error.getMessage)
      case Right(user) => println(user.username)
    }
  }
}

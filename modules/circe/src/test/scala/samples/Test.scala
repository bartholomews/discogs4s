package samples

import cats.implicits._
import io.bartholomews.discogs4s.entities.DiscogsUsername
import io.bartholomews.discogs4s.{DiscogsClient, DiscogsOAuthClient}
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth._
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.AccessToken
import sttp.client3.{HttpURLConnectionBackend, Identity, ResponseException, SttpBackend, UriContext}

object Test {
  // $COVERAGE-OFF$

  val userAgent: UserAgent = UserAgent(
    appName = "bidwish",
    appVersion = Some("0.0.1-SNAPSHOT"),
    appUrl = Some("com.github.bartholomews")
  )

  val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  val consumer: Consumer =
    Consumer(key = sys.env("MUSICGENE_DISCOGS_CONSUMER_KEY"), secret = sys.env("MUSICGENE_DISCOGS_CONSUMER_SECRET"))

  val callback: RedirectUri = RedirectUri(uri"https://bartholomews.io/callback")

  val discogsAuthClient: DiscogsOAuthClient[Identity] =
    DiscogsClient.oAuth(Test.userAgent, consumer)(backend)
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object NoAuth extends App {
  // $COVERAGE-OFF$

  import Test._
  import io.bartholomews.discogs4s.circe.codecs._

  val client = DiscogsClient.authDisabled(userAgent)(backend)
  client.users.getUserProfile(DiscogsUsername("_.bartholomews")).body.fold(println, println)

  // $COVERAGE-ON$
}

/*
    [Authenticated calls with personal token]
 */
object Auth1 extends App {
  // $COVERAGE-OFF$

  import Test._
  import io.bartholomews.discogs4s.circe.codecs._

  val discogsClient = DiscogsClient.personal(
    userAgent,
    AccessToken("???")
  )(backend)

  discogsClient.users.me.body
    .fold(println, println)
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
    [Only Consumer key/secret]
      Not authenticated, but should give better rate limit and expose images
 */
object ClientCredentialsFlow extends App {
  // $COVERAGE-OFF$

  import Test._
  import io.bartholomews.discogs4s.circe.codecs._

  val client = DiscogsClient.clientCredentials(userAgent, consumer)(backend)

  client.users
    .getUserProfile(DiscogsUsername("_.bartholomews"))
    .headers
    .foreach(println)

  println()
  println("~" * 50)
  println()
  // $COVERAGE-ON$
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
    [OAUTH STEP 1] => Retrieve a `Request Token`;
 */
object Step1RetrieveRequestToken extends App {
  // $COVERAGE-OFF$
  val requestToken: Either[ResponseException[String, Exception], TemporaryCredentials] = Test.discogsAuthClient.auth
    .getRequestToken(
      TemporaryCredentialsRequest(
        Test.consumer,
        Test.callback,
        SignatureMethod.SHA1
      )
    )
    .body
    .bimap(
      err => {
        println(err)
        err
      },
      token => {
        println(token)
        println(token.resourceOwnerAuthorizationRequest)
        token
      }
    )
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/*
    [OAUTH STEP 2] =>
        Use RequestToken and oauth_token + verifier from `RequestTokenResponse.callback`;
        Retrieve `AccessTokenCredentials`
 */
object Step2RetrieveAccessToken extends App {
  // $COVERAGE-OFF$

  val temporaryCredentials: TemporaryCredentials =
    TemporaryCredentials(
      Consumer("???", "???"),
      Token("???", "???"),
      callbackConfirmed = true,
      ResourceOwnerAuthorizationUri(uri"https://discogs.com/oauth/authorize")
    )

  Test.discogsAuthClient.auth
    .fromUri(
      uri"https://bartholomews.io/callback?oauth_token=???&oauth_verifier=???",
      temporaryCredentials,
      SignatureMethod.SHA1
    )
    .body
    .fold(println, println)

  // $COVERAGE-ON$
}

/*
    [OAUTH STEP 3] =>
        Use the access token to make authorized calls
 */
object Step3UseAccessToken extends App {
  // $COVERAGE-OFF$

  import io.bartholomews.discogs4s.circe.codecs._

  val accessToken: AccessTokenCredentials = AccessTokenCredentials(
    Token("???", "???"),
    Test.consumer,
    SignatureMethod.SHA1
  )
  //  Main.discogsClient.auth.me(accessToken).unsafeRunSync().entity
  Test.discogsAuthClient.users.me(accessToken).body.fold(println, println)

  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

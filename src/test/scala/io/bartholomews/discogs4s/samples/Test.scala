package io.bartholomews.discogs4s.samples

import cats.implicits._
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.discogs4s.DiscogsClient.personalToken
import io.bartholomews.discogs4s.entities.Username
import io.bartholomews.fsclient.core.config.UserAgent
import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.{Consumer, SignatureMethod, Token}
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.{AccessToken, RedirectUri}
import io.bartholomews.fsclient.core.oauth.{
  AccessTokenCredentials,
  CustomAuthorizationHeader,
  ResourceOwnerAuthorizationUri,
  SignerV1,
  TemporaryCredentialsRequest
}
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, ResponseError, SttpBackend, UriContext}

object Test {
  // $COVERAGE-OFF$

  val userAgent: UserAgent = UserAgent(
    appName = "bidwish",
    appVersion = Some("0.0.1-SNAPSHOT"),
    appUrl = Some("com.github.bartholomews")
  )

  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  val consumer: Consumer =
    Consumer(key = sys.env("MUSICGENE_DISCOGS_CONSUMER_KEY"), secret = sys.env("MUSICGENE_DISCOGS_CONSUMER_SECRET"))

  val callback: RedirectUri = RedirectUri(uri"https://bartholomews.io/callback")

  val discogsClient: DiscogsClient[Identity, SignerV1] =
    DiscogsClient.clientCredentials(Test.userAgent, consumer)
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object NoAuth extends App {
  // $COVERAGE-OFF$

  import Test._

  val client = DiscogsClient.basic(userAgent)
  client.users.getSimpleUserProfile(Username("_.bartholomews")).body.fold(println, println)

  // $COVERAGE-ON$
}

/*
    [Authenticated calls with personal token]
 */
object Auth1 extends App {
  // $COVERAGE-OFF$

  import Test._

  implicit val signer: CustomAuthorizationHeader =
    personalToken(AccessToken("???"))

  val discogsClient = DiscogsClient.personal(Test.userAgent, signer)

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

  Test.discogsClient.users
    .getSimpleUserProfile(Username("_.bartholomews"))
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
  val requestToken: Either[ResponseError[Exception], TemporaryCredentials] = Test.discogsClient.auth
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

  Test.discogsClient.auth
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

  implicit val accessToken: AccessTokenCredentials = AccessTokenCredentials(
    Token("???", "???"),
    Test.consumer,
    SignatureMethod.SHA1
  )
  //  Main.discogsClient.auth.me(accessToken).unsafeRunSync().entity
  Test.discogsClient.users.me.body.fold(println, println)

  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package io.bartholomews.discogs4s

import cats.effect.{ContextShift, IO, Resource}
import io.bartholomews.discogs4s.endpoints.GetSimpleUserProfile
import io.bartholomews.discogs4s.entities.Username
import io.bartholomews.fsclient.client.FClientNoAuth
import io.bartholomews.fsclient.config.UserAgent
import io.bartholomews.fsclient.entities.oauth._
import io.bartholomews.fsclient.implicits._
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.{Consumer, Token}

import scala.concurrent.ExecutionContext

object Test {
  // $COVERAGE-OFF$
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  val userAgent: UserAgent = UserAgent(
    appName = "bidwish",
    appVersion = Some("0.0.1-SNAPSHOT"),
    appUrl = Some("com.github.bartholomews")
  )

  val consumer: Consumer =
    Consumer(key = sys.env("DISCOGS_CONSUMER_KEY"), secret = sys.env("DISCOGS_CONSUMER_SECRET"))

  val callback: Uri =
    Uri.unsafeFromString("https://bartholomews.io/callback")

  val basicSignature: SignerV1 = ClientCredentials(consumer)

  val discogsClient = new DiscogsClient[IO](Test.userAgent, basicSignature)
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object NoAuth extends App {
  // $COVERAGE-OFF$

  import Test._

  val client = FClientNoAuth[IO](Test.userAgent)

  println {
    GetSimpleUserProfile(Username("_.bartholomews")).runWith(client).unsafeRunSync()
  }

  // $COVERAGE-ON$
}

/*
    [OAuth with token already injected in client]
 */
object Auth1 extends App {
  // $COVERAGE-OFF$

  import Test._
  val token = Token("TODO", "TODO")
  implicit val signer: SignerV1 = AccessTokenCredentials(token, Test.consumer)
  val discogsClient = new DiscogsClient[IO](Test.userAgent, signer)

  // FIXME: This is useless since the token does not belong to the user,
  //  is misleading and will get back `simpleUser`:
  //  should probably throw an error
  discogsClient.users
    .getAuthenticateUserProfile(Username("_.bartholomews"))
    .unsafeRunSync()
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
    [Unauthorised flow with simple client]
 */
object UnauthorisedFlow extends App {
  // $COVERAGE-OFF$

  Test.discogsClient.users
    .getSimpleUserProfile(Username("_.bartholomews"))
    .unsafeRunSync()

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
  println {
    Test.discogsClient.auth
      .getRequestToken(TemporaryCredentialsRequest(Test.consumer, Test.callback))
      .unsafeRunSync()
      .entity
      .map(_.callback)
  }
  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/*
    [OAUTH STEP 2] =>
        Get RequestToken and verifier from `RequestTokenResponse.callback`;
        Retrieve a `DiscogsOAuthClient`
 */
object Step2RetrieveAccessToken extends App {
  // $COVERAGE-OFF$

  //PKnGcslTmVELENLFrVyEpLgcKuqWGxSwJRTSirhe

  import cats.implicits._
  val oAuthToken = Token(
    "???",
    "???"
  )
  val verifier: String = "???"

  val requestToken = RequestTokenCredentials(oAuthToken, verifier, Test.consumer)

  val accessToken: AccessTokenCredentials = Test.discogsClient.auth
    .getAccessToken(requestToken)
    .unsafeRunSync()
    .entity
    .leftMap(println)
    .getOrElse(throw new Exception("WOOOPS!"))

  println("~" * 50)
  println(accessToken)
  println("~" * 50)
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
    Test.consumer
  )
  //  Main.discogsClient.auth.me(accessToken).unsafeRunSync().entity
  Test.discogsClient.users
    .getAuthenticateUserProfile(Username("_.bartholomews"))(accessToken)
    .unsafeRunSync()

  // $COVERAGE-ON$
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

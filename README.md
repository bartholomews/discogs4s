[![Build Status](https://travis-ci.org/bartholomews/discogs4s.svg?branch=master)](https://travis-ci.org/bartholomews/discogs4s)
[![codecov](https://codecov.io/gh/bartholomews/discogs4s/branch/master/graph/badge.svg)](https://codecov.io/gh/bartholomews/discogs4s)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# discogs4s
Early stage *Discogs* client with the *Typelevel* stack

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13)

```
libraryDependencies += "io.bartholomews" %% "discogs4s" % "0.0.1"
```

### Simple client

```scala
  import cats.effect.{ContextShift, IO, Resource}
  import io.bartholomews.discogs4s.endpoints.GetSimpleUserProfile
  import io.bartholomews.discogs4s.entities.Username
  import io.bartholomews.fsclient.client.FClientNoAuth
  import io.bartholomews.fsclient.config.UserAgent
  import org.http4s.client.Client
  import org.http4s.client.blaze.BlazeClientBuilder
  import scala.concurrent.ExecutionContext

  // needed for importing empty entity encoder and json decoder
  import io.bartholomews.fsclient.implicits._

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  val userAgent = UserAgent(appName = "my-app", appVersion = None, appUrl = None)

  // create a basic client ready to make (unsigned) requests
  val discogsClient = FClientNoAuth[IO](userAgent)

  println {
    // run a request with your client
    GetSimpleUserProfile(Username("_.bartholomews")).runWith(discogsClient).unsafeRunSync()
  }
```

### OAuth

Read  the discogs [OAuth Flow](https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow)

#### Client Credentials 

If you want to use consumer credentials, add them in your `application.conf`:
```
user-agent {
    app-name = "your-consumer-app"
    app-version = "0.0.1-SNAPSHOT (optional)"
    app-url = "your-app-url (optional)"
}

discogs {
    consumer {
        key: ${?APP_CONSUMERY_KEY}
        secret: ${?APP_CONSUMER_SECRET}
    }
}
```

This way you can create a client with *Client Credentials*:
```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
  import scala.concurrent.ExecutionContext

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val discogsClient: DiscogsClient =
    DiscogsClient.unsafeFromConfig(SignerType.BasicSignature)
```

This client will sign by default with consumer key/secret, so you can benefit
from higher rate limiting.

#### Personal access token

If you have a personal access token you could just create a client with that:
```
user-agent {
    app-name = "your-consumer-app"
    app-version = "0.0.1-SNAPSHOT (optional)"
    app-url = "your-app-url (optional)"
}

discogs {
    consumer {
        key: ${?APP_CONSUMERY_KEY}
        secret: ${?APP_CONSUMER_SECRET}
    }
    access-token {
        value: ${?TOKEN_VALUE}
        secret: ${?TOKEN_SECRET} 
    }
}
```

This way you can create a client with *Personal access token*:
```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
  import scala.concurrent.ExecutionContext

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val discogsClient: DiscogsClient =
    DiscogsClient.unsafeFromConfig(SignerType.TokenSignature)
```

You could also create a client manually passing directly `UserAgent` and `Signer`.

#### Full OAuth 1.0a with access token/secret
```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{RequestToken, Username}
  import io.bartholomews.fsclient.entities.ErrorBody
  import io.bartholomews.fsclient.entities.oauth.AccessTokenCredentials
  import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
  import org.http4s.Uri

  import scala.concurrent.ExecutionContext

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val discogsClient: DiscogsClient =
    DiscogsClient.unsafeFromConfig(SignerType.BasicSignature)
  
  // the uri to be redirected after the user will grant permissions for your app
  val callbackUri =
    Uri.unsafeFromString("http://localhost:9000/discogs/callback")
  
  val maybeRequestToken: Either[ErrorBody, RequestToken] = discogsClient.auth
    .getRequestToken(discogsClient.temporaryCredentialsRequest(callbackUri))
    .unsafeRunSync()
    .entity

  // you should store the request token entity somewhere  
  val requestToken: RequestToken = maybeRequestToken.right.get
  // the discogs uri your app should redirect to, where the user will grant permissions
  val discogsRedirectUri: Uri = requestToken.callback

  /*
    After you have sent the user to `discogsRedirectUri`,
    the user will be redirected to `callbackUri`: the url will have
    query parameters with the token key and verifier;
    it doesn't seem to have the token secret, 
    that's why you need to store the request token in the previous step 
  */
  val uriFromCallback = 
    callbackUri.withQueryParam("oauth_token", "AAABBB")
    callbackUri.withQueryParam("oauth_verifier", "ZZZZZ")
  
  // finally get the access token credentials;
  // you could serialize it in the client session cookies
  // or store it somewhere: it doesn't expire;
  val accessToken: AccessTokenCredentials =
    discogsClient.auth.fromUri(requestToken, callbackUri)
      .unsafeRunSync()
      .entity
      .right
      .get
  
   // you need to provide an accessToken to make user-authenticated calls
   discogsClient.users  
    .getAuthenticateUserProfile(Username("_.bartholomews"))(accessToken)
    .unsafeRunSync()

```

### Implemented endpoints:

- *AuthApi*
    - [`getRequestToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-request-token-url)
    - [`getAccessToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-access-token-url)
    - [`me`](https://www.discogs.com/developers/#page:user-identity)
    
- *ArtistsApi*
    - [`getArtistsReleases`](https://www.discogs.com/developers/#page:database,header:database-artist-releases)
    
- *UsersApi*
    - [`getSimpleUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get)    
    - [`getAuthenticateUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get)    
    - [`updateUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post)    
    
### Contributing

Any request / issue / help / PR is most welcome.

### CI/CD Pipeline

This project is using [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release) plugin:
 - Every push to master will trigger a snapshot release.  
 - In order to trigger a regular release you need to push a tag:
 
    ```bash
    ./scripts/release.sh v1.0.0
    ```
 
 - If for some reason you need to replace an older version (e.g. the release stage failed):
 
    ```bash
    TAG=v1.0.0
    git push --delete origin ${TAG} && git tag --delete ${TAG} \
    && ./scripts/release.sh ${TAG}
    ```
[![Actions Status](https://github.com/bartholomews/discogs4s/workflows/build/badge.svg)](https://github.com/bartholomews/discogs4s/actions)
[![codecov](https://codecov.io/gh/bartholomews/discogs4s/branch/master/graph/badge.svg)](https://codecov.io/gh/bartholomews/discogs4s)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# discogs4s
Early stage *Discogs* client wrapping [sttp](https://sttp.softwaremill.com/en/stable)

The client is using the library [fsclient](https://github.com/bartholomews/fsclient)
which is a wrapper around sttp with circe and OAuth handling.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13)

```
libraryDependencies += "io.bartholomews" %% "discogs4s" % "0.1.0"
```

## Simple client

```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  private val userAgent = UserAgent(appName = "my-app", appVersion = None, appUrl = None)

  // create a basic client ready to make (unsigned) requests
  private val client = DiscogsClient.basic(userAgent)

  // run a request with your client
  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
```

## OAuth

Read  the discogs [OAuth Flow](https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow)

### Client Credentials 

If you want to use consumer credentials from config, add them in your `application.conf`:
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

Then you can create a client with *Client Credentials*:
```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.circe
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()
  
  // you could also pass the credentials directly in `DiscogsClient.clientCredentials`
  private val client = DiscogsClient.clientCredentialsFromConfig

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
```

This client will sign by default with consumer key/secret, so you can benefit
from higher rate limiting.

### Personal access token

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
    access-token:${?PERSONAL_TOKEN_VALUE}
}
```

This way you can create a client with *Personal access token*:
```scala
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
```

You could also create a client manually passing directly `UserAgent` and `Signer`.

### Full OAuth 1.0a with access token/secret
```scala
  import io.bartholomews.discogs4s.DiscogsClient
  import io.bartholomews.discogs4s.entities.Username
  import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RedirectUri
  import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, SignerV1, TemporaryCredentialsRequest}
  import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend, UriContext}
  import sttp.model.Uri

  type F[X] = Identity[X]
  implicit val backend: SttpBackend[F, Nothing, NothingT] = HttpURLConnectionBackend()

  val discogsClient: DiscogsClient[F, SignerV1] =
    DiscogsClient.clientCredentialsFromConfig

  // the uri to be redirected after the user will grant permissions for your app
  private val redirectUri = RedirectUri(uri"http://localhost:9000/discogs/callback")

  val temporaryCredentialsRequest: TemporaryCredentialsRequest =
    discogsClient.temporaryCredentialsRequest(redirectUri)

  for {
    temporaryCredentials <- discogsClient.auth.getRequestToken(temporaryCredentialsRequest).body

    /*
      After the user accept/reject permissions for your app,
      they will be redirected to `redirectUri`: the url will have
      query parameters with the token key and verifier;
      it doesn't seem to have the token secret,
      that's why you need to keep the temporary credentials in the previous step
     */
    resourceOwnerAuthorizationUriResponse: Uri = redirectUri.value.params(
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
    implicit val token: AccessTokenCredentials = accessToken
    // you need to provide an accessToken to make user-authenticated calls
    discogsClient.users.getAuthenticateUserProfile(Username("_.bartholomews"))
  }
```

## Implemented endpoints:

- **AuthApi**
    - [`getRequestToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-request-token-url)
    - [`getAccessToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-access-token-url)
    - [`me`](https://www.discogs.com/developers/#page:user-identity)
    
- **ArtistsApi**
    - [`getArtistsReleases`](https://www.discogs.com/developers/#page:database,header:database-artist-releases)
    
- **UsersApi**
    - [`getSimpleUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get)    
    - [`getAuthenticateUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get)    
    - [`updateUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post)    
    
## Contributing

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
[![Actions Status](https://github.com/bartholomews/discogs4s/workflows/build/badge.svg)](https://github.com/bartholomews/discogs4s/actions)
[![Coverage Status](https://coveralls.io/repos/github/bartholomews/discogs4s/badge.svg?branch=master)](https://coveralls.io/github/bartholomews/discogs4s?branch=master)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# discogs4s
Early stage *Discogs* client wrapping [sttp](https://sttp.softwaremill.com/en/latest)

The client is using the library [fsclient](https://github.com/bartholomews/fsclient)
which is a wrapper around sttp with circe and OAuth handling.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bartholomews/discogs4s_2.13)

```
// circe codecs
libraryDependencies += "io.bartholomews" %% "discogs4s-circe" % "<LATEST_VERSION>"
// play codecs
libraryDependencies += "io.bartholomews" %% "discogs4s-play" % "<LATEST_VERSION>"
// no codecs (you need to provide your own)
libraryDependencies += "io.bartholomews" %% "discogs4s-core" % "<LATEST_VERSION>"
```

*Please note that the following docs are based on the latest snapshot version*  
(you need to have the following in your `build.sbt` in case):
```
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```

## Discogs clients

You can instantiate different discogs clients depending on the required [flow](https://www.discogs.com/developers/#page:authentication,header:authentication-discogs-auth-flow)

### Basic client
This is the most basic client with no credentials and low rate limits.

* Credentials in request ? *None*
* Rate limiting          ? 🐢 *Low tier*
* Image URLs             ? ❌ *No*
* Authenticated as user  ? ❌ *No*

```scala
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsSimpleClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.AuthDisabled
  import io.circe
  import pureconfig.ConfigReader.Result
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

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
   */
  // create a basic client ready to make (unsigned) requests:
  private val client = DiscogsClient.authDisabled.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsSimpleClient[F, AuthDisabled.type]] =
    DiscogsClient.authDisabled.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `Consumer` directly
  private val explicitClient = DiscogsClient.authDisabled.apply(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>"))
  )(backend)

  // run a request with your client
  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
```

### Client Credentials
This client has higher rate limits, but still cannot call user-authenticated endpoints.
You need to provide consumer key/secret in (developer settings)[https://www.discogs.com/settings/developers]
(at least in theory, currently any dummy consumer key/secret is getting the higher rate limit x--(ツ)--x)

* Credentials in request ? *Only Consumer key/secret*
* Rate limiting          ? 🐰 *High tier*
* Image URLs             ? ✔ *Yes*
* Authenticated as user  ? ❌ *No*

```scala
  import io.bartholomews.discogs4s.entities.{SimpleUser, Username}
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsSimpleClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.SignerV1
  import io.bartholomews.fsclient.core.oauth.v1.OAuthV1.Consumer
  import io.circe
  import pureconfig.ConfigReader.Result
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

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
  private val client = DiscogsClient.clientCredentials.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsSimpleClient[F, SignerV1]] = DiscogsClient.clientCredentials.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `Consumer` directly
  private val explicitClient = DiscogsClient.clientCredentials.apply(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    Consumer(key = "<YOUR_CONSUMER_KEY>", secret = "<YOUR_CONSUMER_SECRET>")
  )(backend)

  val response: F[SttpResponse[circe.Error, SimpleUser]] =
    client.users.getSimpleUserProfile(Username("_.bartholomews"))
```

### Personal access token
This client has higher rate limits and can also make user-authenticated calls (for your user only).
You need to provide your personal access token from [developer settings](https://www.discogs.com/settings/developers)

* Credentials in request ? *Personal access token*
* Rate limiting          ? 🐰 *High tier*
* Image URLs             ? ✔ *Yes*
* Authenticated as user  ? ✔ *Yes, for token holder only* 👩

```scala
  import io.bartholomews.discogs4s.entities.UserIdentity
  import io.bartholomews.discogs4s.{DiscogsClient, DiscogsPersonalClient}
  import io.bartholomews.fsclient.core.config.UserAgent
  import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
  import io.bartholomews.fsclient.core.oauth.OAuthSigner
  import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.AccessToken
  import io.circe
  import pureconfig.ConfigReader.Result
  import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

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
    access-token: "<YOUR_PERSONAL_ACCESS_TOKEN>"
  }
   */
  private val client = DiscogsClient.personal.unsafeFromConfig(backend)
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsPersonalClient[F, OAuthSigner]] =
    DiscogsClient.personal.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `AccessToken` directly
  private val explicitClient = DiscogsClient.personal(
    UserAgent(appName = "<YOUR_APP_NAME>", appVersion = Some("<YOUR_APP_VERSION>"), appUrl = Some("<YOUR_APP_URL>")),
    AccessToken(value = "<YOUR_PERSONAL_ACCESS_TOKEN>")
  )(backend)

  // You can make authenticated (for your user only) calls
  val response: F[SttpResponse[circe.Error, UserIdentity]] = client.users.me
```

### Full OAuth 1.0a with access token/secret
This client is for making calls on behalf of any authenticated user which granted permissions for your app via OAuth 1.0

* Credentials in request ? *Full OAuth 1.0a with access token/secret*
* Rate limiting          ? 🐰 *High tier*
* Image URLs             ? ✔ *Yes*
* Authenticated as user  ? ✔ *Yes, on behalf of any user* 🌍

```scala
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
  // you can also create a safe client from config
  private val safeClient: Result[DiscogsOAuthClient[F]] = DiscogsClient.oAuth.fromConfig(backend)
  // you can also create a client providing `UserAgent` and `Consumer` directly
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
```

## Implemented endpoints:

- **AuthApi****
    - [`getRequestToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-request-token-url)
    - [`getAccessToken`](https://www.discogs.com/developers/#page:authentication,header:authentication-access-token-url)
    
- **DatabaseApi**
    - [`getArtistsReleases`](https://www.discogs.com/developers/#page:database,header:database-artist-releases)
    
- **UsersApi**
    - [`me`](https://www.discogs.com/developers/#page:user-identity)*
    - [`getUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get)    
    - [`updateUserProfile`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-post)*    
    - [`getUserSubmissions`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-user-submissions)     
    - [`getUserContributions`](https://www.discogs.com/developers/#page:user-identity,header:user-identity-user-contributions)    
    
[*]  *Available only in `DiscogsOAuthClient` and `DiscogsPersonalClient`*  
[**] *Available only in `DiscogsOAuthClient`*
    
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
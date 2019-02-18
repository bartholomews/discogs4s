package client.discogs

import cats.data.EitherT
import cats.effect.{ContextShift, IO, Resource}
import client.discogs.api._
import client.discogs.entities._
import client.effect4s.IOClient
import client.effect4s.config.{OAuthConfig, OAuthConsumer}
import client.effect4s.entities.{HttpResponse, ResponseError}
import io.circe.Decoder
import org.http4s.Method
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

// https://http4s.org/v0.19/streaming/
class DiscogsSimpleClient(consumerConfig: OAuthConsumer)
                         (implicit ec: ExecutionContext) extends IOClient(consumerConfig)
  with DiscogsOAuthPipes {

  def this()(implicit ec: ExecutionContext) = this(OAuthConfig.oAuthConsumer)

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  // ===================================================================================================================
  // GET FIXME: OAuthClient need to call `fetchJson` with extra Token param
  // ===================================================================================================================

  private case class DiscogsIO[T <: DiscogsEntity](private val endpoint: DiscogsEndpoint[T])(implicit decode: Decoder[T]) {
    def apply: IOResponse[T] = fetchJson(endpoint.uri, endpoint.method)
  }

  // ===================================================================================================================
  // OAUTH
  // ===================================================================================================================

  case object RequestToken {
    def get: IOResponse[RequestTokenResponse] =
      fetchPlainText(AuthorizeUrl.uri)
  }

  private[client] case object AccessToken {
    def get(request: AccessTokenRequest): IOResponse[AccessTokenResponse] =
      fetchPlainText(request.uri, Method.POST, accessToken = Some(request))
  }

  def getOAuthClient(request: AccessTokenRequest): IO[Either[ResponseError, DiscogsOAuthClient]] = (for {
    accessToken <- EitherT(AccessToken.get(request).map(_.entity))
    res <- EitherT.right[ResponseError](IO.pure(new DiscogsOAuthClient(consumerConfig, accessToken)))
  } yield res).value

  // ===================================================================================================================
  // ARTISTS API
  // ===================================================================================================================

  /**
    * https://www.discogs.com/developers/#page:database,header:database-artist-releases
    *
    * Get an artist’s releases
    *
    * @param artistId The Artist ID
    *                 TODO Sort items by this field: `year`, `title`, `format`
    * @param page
    * @param perPage
    * @return
    */
  def getArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): IO[HttpResponse[PaginatedReleases]] = {
    DiscogsIO(ArtistsReleases(artistId, page, perPage)).apply
  }

  // ===================================================================================================================
  // USER API // https://www.discogs.com/developers/#page:user-identity
  // ===================================================================================================================

  /**
    * https://www.discogs.com/developers/#page:user-identity,header:user-identity-profile-get
    *
    * Retrieve a user by username.
    * If authenticated as the requested user, the email key will be visible,
    * and the num_list count will include the user’s private lists.
    *
    * If authenticated as the requested user or the user’s collection/wantlist is public,
    * the num_collection / num_wantlist keys will be visible.
    *
    * @param username The username of whose profile you are requesting.
    * @return `String`
    */
  def getUserProfile(username: String): IO[HttpResponse[SimpleUser]] = {
    DiscogsIO(GetSimpleUserProfile(username)).apply
  }
}
package client.discogs

import cats.data.EitherT
import cats.effect.{ContextShift, IO, Resource}
import client.effect4s.HttpTypes
import client.effect4s.entities.{HttpResponse, ResponseError}
import client.io.IOClient
import client.discogs.api._
import client.discogs.entities._
import client.discogs.utils.Config
import client.discogs.utils.Config.DiscogsConsumer
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication

class DiscogsSimpleClient(consumerConfig: DiscogsConsumer)
                         (implicit ec: ExecutionContext) extends DiscogsRest(consumerConfig) with HttpTypes {

  def this()(implicit ec: ExecutionContext) = this(Config.consumer)

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  // ===================================================================================================================
  // GET FIXME: OAuthClient need to call `fetchJson` with extra Token param
  // ===================================================================================================================

  private case class GET[T <: DiscogsEntity](private val api: DiscogsApi[T])
                                            (implicit decode: Decoder[T]) extends IOClient[T] {

    def io: IO[HttpResponse[T]] = resource.use(fetchJson(_)(getRequest(api.uri)))
  }

  // ===================================================================================================================
  // OAUTH
  // ===================================================================================================================

  case object RequestToken extends DiscogsOAuthPipes with IOClient[RequestTokenResponse] {
    def get: IOResponse[RequestTokenResponse] =
      resource.use(fetchPlainText(_)(getRequest(AuthorizeUrl.uri)))
  }

  private[client] case object AccessToken extends DiscogsOAuthPipes with IOClient[AccessTokenResponse] {
    def get(request: AccessTokenRequest): IOResponse[AccessTokenResponse] =
      resource.use(fetchPlainText(_)(postRequest(request.uri), Some(request)))
  }

  def getOAuthClient(request: AccessTokenRequest): IO[Either[ResponseError, DiscogsOAuthClient]] = (for {
    accessToken <- EitherT(AccessToken.get(request).map(_.entity))
    res <- EitherT.right[ResponseError](IO.pure(new DiscogsOAuthClient(consumerConfig, accessToken)))
  } yield res).value

  // ===================================================================================================================
  // ARTISTS API
  // ===================================================================================================================

  def getArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): IO[HttpResponse[PaginatedReleases]] = {
    GET(ArtistsReleases(artistId, page, perPage)).io
  }

  // ===================================================================================================================
}
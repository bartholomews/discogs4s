package client.discogs

import cats.data.EitherT
import cats.effect.{ContextShift, IO, Resource}
import client.discogs.api._
import client.discogs.entities._
import client.discogs.utils.Config
import client.discogs.utils.Config.DiscogsConsumer
import client.effect4s.{HttpRest, IOClient}
import client.effect4s.entities.{HttpResponse, ResponseError}
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

// https://http4s.org/v0.19/streaming/
class DiscogsSimpleClient(consumerConfig: DiscogsConsumer)
                         (implicit ec: ExecutionContext) extends HttpRest(consumerConfig)
  with IOClient
  with DiscogsOAuthPipes {

  def this()(implicit ec: ExecutionContext) = this(Config.consumer)

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  // ===================================================================================================================
  // GET FIXME: OAuthClient need to call `fetchJson` with extra Token param
  // ===================================================================================================================

  private case class GET[T <: DiscogsEntity](private val endpoint: DiscogsEndpoint[T])(implicit decode: Decoder[T]) {
    def io: IOResponse[T] = getJson(getRequest(endpoint.uri))
  }

  // ===================================================================================================================
  // OAUTH
  // ===================================================================================================================

  case object RequestToken {
    def get: IOResponse[RequestTokenResponse] =
      getPlainText(getRequest(AuthorizeUrl.uri))
  }

  private[client] case object AccessToken {
    def get(request: AccessTokenRequest): IOResponse[AccessTokenResponse] =
      getPlainText(postRequest(request.uri), Some(request))
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
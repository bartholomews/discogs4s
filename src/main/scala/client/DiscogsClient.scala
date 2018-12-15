package client

import api._
import cats.effect.{ContextShift, Effect, IO, Resource}
import client.http.{HttpResponse, IOClient, OAuthClient}
import client.utils.{Config, ConsumerConfig, HttpTypes}
import entities.{AccessTokenResponse, DiscogsEntity, PaginatedReleases, RequestTokenResponse}
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.oauth1.Consumer
import org.http4s.{Header, Headers, Method, Request, Uri}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication

class DiscogsClient(consumerClient: Option[ConsumerConfig] = None)
                   (ec: ExecutionContext) extends HttpTypes {

  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val resource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](ec).resource

  // ===================================================================================================================
  // CONFIG TODO: Maybe should be moved in a trait to be more nicely shared with OAuthClient instead of extending ?
  // ===================================================================================================================

  //  TODO allow only consumer_config in application.conf, so you can move OAuth setup defs in a companion object
  private[client] val consumerConfig = consumerClient.getOrElse(Config.CONSUMER_CONFIG) // todo handle error
  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private[client] val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private[client] def request[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  private[client] def postRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.POST)

  private[client] def getRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.GET)

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

  case object AccessToken extends OAuthClient with IOClient[AccessTokenResponse] {
    def get(request: AccessTokenRequest): IOResponse[AccessTokenResponse] =
      resource.use(fetchPlainText(_)(postRequest(request.uri), Some(request)))
  }

  case object RequestToken extends OAuthClient with IOClient[RequestTokenResponse] {
    def get: IOResponse[RequestTokenResponse] =
      resource.use(fetchPlainText(_)(getRequest(AuthorizeUrl.uri)))
  }

  // ===================================================================================================================
  // ARTISTS API
  // ===================================================================================================================

  def getArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): IO[HttpResponse[PaginatedReleases]] = {
    GET(ArtistsReleases(artistId, page, perPage)).io
  }

  // ===================================================================================================================
}
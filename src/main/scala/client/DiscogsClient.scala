package client

import cats.effect.{Effect, IO}
import api.{AccessTokenRequest, ArtistsReleases, AuthorizeUrl, DiscogsApi}
import client.http.{IOClient, OAuthClient}
import entities.{AccessTokenResponse, DiscogsEntity, PaginatedReleases, RequestTokenResponse}
import io.circe.Decoder
import org.http4s.client.oauth1.Consumer
import org.http4s.{Header, Headers, Method, Request, Uri}
import client.utils.{Config, ConsumerConfig, HttpTypes}

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

// https://http4s.org/v0.19/streaming/
// TODO
// DiscogsAuthClient which has oauth_token and secret vals, can be created only via:
// https://www.discogs.com/developers/#page:authentication

case class DiscogsClient(consumerClient: Option[ConsumerConfig] = None) extends HttpTypes {

  // ===================================================================================================================
  // CONFIG
  // ===================================================================================================================

  private val consumerConfig = consumerClient.getOrElse(Config.CONSUMER_CONFIG) // todo handle error
  private implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private val USER_AGENT = Headers {
    Header("User-Agent", consumerConfig.userAgent)
  }

  private def request[F[_] : Effect](uri: Uri): Request[F] = Request[F]()
    .withUri(uri)
    .withHeaders(USER_AGENT)

  private def postRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.POST)

  private def getRequest[F[_] : Effect](uri: Uri): Request[F] =
    request(uri).withMethod(Method.GET)

  // ===================================================================================================================
  // OAUTH
  // ===================================================================================================================

  case object AccessToken extends OAuthClient with IOClient[AccessTokenResponse] {
    def get(request: AccessTokenRequest): IOResponse[AccessTokenResponse] =
      fetchPlainText(postRequest(request.uri), Some(request))
  }

  case object RequestToken extends OAuthClient with IOClient[RequestTokenResponse] {
    def get: IOResponse[RequestTokenResponse] =
      fetchPlainText(getRequest(AuthorizeUrl.uri))
  }

  // ===================================================================================================================
  // GET
  // ===================================================================================================================

  case class GET[T <: DiscogsEntity](private val api: DiscogsApi[T])
                                    (implicit decode: Decoder[T]) extends IOClient[T] {

    def io: IO[T] = fetchJson(getRequest(api.uri))

    def ioEither: IO[Either[Throwable, T]] = io.attempt

    def ioTry: IO[Try[T]] = ioEither.map(_.fold(
      throwable => Failure(throwable),
      response => Success(response)
    ))
  }

  // ===================================================================================================================
  // ARTISTS API
  // ===================================================================================================================

  def getArtistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): IO[PaginatedReleases] = {
    GET(ArtistsReleases(artistId, page, perPage)).io
  }

  // ===================================================================================================================
}
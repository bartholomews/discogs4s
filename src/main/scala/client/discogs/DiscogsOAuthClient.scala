package client.discogs

import cats.effect.{IO, Resource}
import client.discogs.api._
import client.discogs.entities.{AccessTokenResponse, AuthenticatedUser, DiscogsEntity, UserIdentity}
import client.effect4s.IOClient
import client.effect4s.config.OAuthConsumer
import client.effect4s.entities.HttpResponse
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.client.oauth1.Consumer

// TODO remake this and accessToken private
class DiscogsOAuthClient(val consumerConfig: OAuthConsumer,
                         val accessToken: AccessTokenResponse)
                        (implicit resource: Resource[IO, Client[IO]])
  extends IOClient(consumerConfig)
    with DiscogsOAuthPipes {

  private[client] implicit val consumer: Consumer = Consumer(consumerConfig.key, consumerConfig.secret)

  private case class DiscogsIO[T <: DiscogsEntity](private val endpoint: DiscogsEndpoint[T])(implicit decode: Decoder[T]) {
    def apply: IOResponse[T] = fetchJson(endpoint.uri, endpoint.method, accessToken = Some(accessToken))
  }

  case object Me {
    def apply()(implicit decoder: Decoder[UserIdentity]): IOResponse[UserIdentity] = DiscogsIO(Identity).apply
  }

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
  def getUserProfile(username: String): IO[HttpResponse[AuthenticatedUser]] = {
    DiscogsIO(GetAuthenticatedUserProfile(username)).apply
  }

  // TODO
  def updateUserProfile(username: String, location: String): IO[HttpResponse[AuthenticatedUser]] = {
    DiscogsIO(UpdateUserProfile(username, location)).apply
  }

}
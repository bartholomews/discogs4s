package client

import discogs.api.AccessTokenRequest
import cats.data.EitherT
import cats.effect.IO
import client.effect4s.entities.ResponseError
import discogs.{DiscogsOAuthClient, DiscogsSimpleClient}
import org.http4s.Uri
import org.scalatest.Matchers
import server.MockServerWordSpec

// http://blog.shangjiaming.com/2018/01/04/http4s-intorduction/
// https://www.lewuathe.com/wiremock-in-scala.html
class DiscogsDiscogsOAuthClientSpec
  extends MockServerWordSpec
    with MockClientConfig
    with Matchers {

    "Discogs OAuth Client" when {

      def getOAuthClient(client: DiscogsSimpleClient, verifier: String): IO[Either[ResponseError, DiscogsOAuthClient]] = {
        (for {
          req <- EitherT(client.RequestToken.get.map(_.entity))
          res <- EitherT(client.getOAuthClient(AccessTokenRequest(req.token, verifier)))
        } yield res).value
      }

      "the client is valid" when {

        def validOAuthClient: DiscogsOAuthClient = {
          val client = getOAuthClient(validClient, validVerifier).unsafeRunSync()
          client shouldBe 'right
          client.right.get
        }

        "initialised with an explicit configuration" should {

          "read the consumer values passed in" in {
            val oAuthClient = validOAuthClient
            oAuthClient.oAuthConsumer.key shouldBe validConsumerKey
            oAuthClient.oAuthConsumer.secret shouldBe validConsumerSecret
          }

          "call `Identity` successfully" in {
            val oAuthClient = validOAuthClient
            val res = oAuthClient.Me().unsafeRunSync().entity
            res shouldBe 'right
            res.right.get.resourceUrl shouldBe Uri.unsafeFromString("https://api.discogs.com/users/example")
          }
        }
      }
    }
}
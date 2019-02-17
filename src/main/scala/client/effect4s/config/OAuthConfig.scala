package client.effect4s.config

object OAuthConfig {

  import pureconfig.generic.auto._

  lazy val oAuthConsumer: OAuthConsumer = pureconfig.loadConfigOrThrow[Config].oauth.consumer

  private case class Config(oauth: OAuthConfig)

  private case class OAuthConfig(consumer: OAuthConsumer)

}

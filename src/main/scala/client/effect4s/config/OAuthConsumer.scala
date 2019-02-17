package client.effect4s.config

case class OAuthConsumer(appName: String,
                         appVersion: Option[String],
                         appUrl: Option[String],
                         key: String,
                         secret: String) {

  private val version = appVersion.map(version => s"/$version").getOrElse("")
  private val url = appUrl.map(url => s" (+$url)").getOrElse("")

  // "name/version +(url)"
  lazy val userAgent: String = s"$appName$version$url"
}

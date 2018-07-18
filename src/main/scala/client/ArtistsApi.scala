package client

import org.http4s.Uri.Scheme
import org.http4s.UriTemplate.PathElm
import org.http4s.{Uri, UriTemplate}
import utils.Config

import scala.util.Try

trait ArtistsApi {

  def artistsReleases(artistId: Int, page: Int = 1, perPage: Int = 2): Try[Uri] = {
    UriTemplate(
      scheme = Some(Scheme.https),
      authority = Some(
        Uri.Authority(host = Uri.RegName(Config.DISCOGS_API))
      ),
      path = List(
        PathElm("artists"),
        PathElm(artistId.toString),
        PathElm("releases")
      )
    )
      .toUriIfPossible
      .map(_
        .withQueryParam("page", page)
        .withQueryParam("per_page", perPage)
      )
  }

}

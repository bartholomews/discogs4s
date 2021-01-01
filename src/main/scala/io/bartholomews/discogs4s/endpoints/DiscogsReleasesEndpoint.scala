//package io.bartholomews.discogs4s.endpoints
//
//import io.bartholomews.discogs4s.endpoints.DiscogsReleasesEndpoint._
//import io.bartholomews.discogs4s.entities.Release
//import io.bartholomews.fsclient.core.http.FsClientSttpExtensions.UriExtensions
//import sttp.model.Uri
//
//// https://www.discogs.com/developers#page:database
//object DiscogsReleasesEndpoint {
//  final val basePath: Uri = DiscogsEndpoint.apiUri / "releases"
//}
//
//// https://www.discogs.com/developers#page:database,header:database-release
///**
// * Get a release
// *
// * @param releaseId The Release ID
// * @param currency  Currency for marketplace data. Defaults to the authenticated users currency.
// *                  Must be one of the following:
// *                  USD GBP EUR CAD AUD JPY CHF MXN BRL NZD SEK ZAR
// *                  TODO [ISO_CCY?]
// *                  TODO => Should these request pass auth token (i.e. FsAuthRequest?) Or not? WTF
// */
//case class GetRelease(releaseId: Long, currency: Option[String])
//    extends FsSimpleJson.Get[Release] {
//
//  baseRequest
//
//  override def uri: Uri = basePath / s"$releaseId" / s"${currency.getOrElse("")}"
//}

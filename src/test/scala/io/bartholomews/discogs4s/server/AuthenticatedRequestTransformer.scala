//package io.bartholomews.discogs4s.server
//
//import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
//import com.github.tomakehurst.wiremock.common.FileSource
//import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
//import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
//import io.bartholomews.discogs4s.MockClientConfig
//
//case object AuthenticatedRequestTransformer extends ResponseDefinitionTransformer
//  with OAuthServer with MockClientConfig {
//
//  override val applyGlobally = false
//
//  override def transform(request: Request,
//                         response: ResponseDefinition,
//                         files: FileSource,
//                         parameters: Parameters): ResponseDefinition = {
//
//    implicit val responseDefinition: ResponseDefinition = response
//
//    val res: ResponseDefinitionBuilder = likeResponse
//      .withHeader("Content-Type", "text/plain")
//
//    oAuthResponseHeaders(request) match {
//
//      case accessTokenResponseRegex(_, key, _, _, _, _, _) =>
//        if(key == validConsumerKey) res.withStatus(200).build()
//        else if (key == consumerWithInvalidSignature) error(401, ErrorMessage.invalidSignature)
//          // FIXME this won't work chained with another transformer which will change the body (e.g. json response):
//        else if (key == consumerGettingUnexpectedResponse) res.withBody(unexpectedResponse).build()
//        else error(401, ErrorMessage.invalidConsumer)
//
//      case _ => res.withStatus(400).build()
//    }
//  }
//
//  override def getName: String = "oauth-request-transformer"
//}
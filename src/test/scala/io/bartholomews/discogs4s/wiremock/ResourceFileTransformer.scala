package io.bartholomews.discogs4s.wiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

case object ResourceFileTransformer extends ResponseDefinitionTransformer {

  override val applyGlobally = false

  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    val requestUrl: String = request.getUrlStripSlashes

    ResponseDefinitionBuilder
      .like(response)
      .but()
      .withBodyFile(s"$requestUrl.json")
      .build()
  }

  override def getName: String = "resource-file-transformer"
}

package server

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

case object ResourceJsonTransformer extends ResponseDefinitionTransformer {

  override val applyGlobally = false

  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    val requestUrl: String = {
      val url = request.getUrl
      if (url.startsWith("/")) url.drop(1) else url
    }

    val res = ResponseDefinitionBuilder.like(response)

    if (response.getStatus != 200) res.build()
    else res.but()
      .withHeader("Content-Type", "application/json")
      .withBodyFile(s"$requestUrl.json")
      .build()
  }

  override def getName: String = "resource-json-transformer"
}
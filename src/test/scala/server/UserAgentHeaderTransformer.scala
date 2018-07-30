package server

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

case object UserAgentHeaderTransformer extends ResponseDefinitionTransformer {
  override def transform(request: Request,
                         response: ResponseDefinition,
                         files: FileSource,
                         parameters: Parameters): ResponseDefinition = {

    ResponseDefinitionBuilder
      .like(response).but()
      .withHeader("User-Agent", "TODO")
      .build()
  }

  override def getName: String = "user-agent-header-transformer"
}
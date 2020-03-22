package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.http.Request

package object server {
  implicit class RequestImplicits(request: Request) {
    implicit def getUrlStripSlashes: String =
      request.getUrl
        .stripPrefix("/")
        .stripSuffix("/")
  }
}

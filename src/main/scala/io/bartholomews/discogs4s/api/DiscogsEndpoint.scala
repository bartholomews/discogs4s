package io.bartholomews.discogs4s.api

import io.bartholomews.discogs4s.utils.Configuration

trait DiscogsEndpoint {
  private[api] val baseUri = Configuration.discogs.baseUri
  private[api] val apiUri = Configuration.discogs.apiUri
}




package io.bartholomews.discogs4s.endpoints

import io.bartholomews.discogs4s.utils.Configuration

trait DiscogsEndpoint {
  private[endpoints] val baseUri = Configuration.discogs.baseUri
  private[endpoints] val apiUri = Configuration.discogs.apiUri
}

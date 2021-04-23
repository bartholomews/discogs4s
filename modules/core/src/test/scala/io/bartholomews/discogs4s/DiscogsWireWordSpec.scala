package io.bartholomews.discogs4s

import io.bartholomews.discogs4s.diff.DiscogsDiffDerivations
import io.bartholomews.scalatestudo.WireWordSpec
import org.scalatest.matchers.should.Matchers

trait DiscogsWireWordSpec extends WireWordSpec with DiscogsDiffDerivations with Matchers {
  override val testResourcesFileRoot = "src/test/resources"
}

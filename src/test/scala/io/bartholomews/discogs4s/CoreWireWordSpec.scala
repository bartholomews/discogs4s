package io.bartholomews.discogs4s

import io.bartholomews.scalatestudo.WireWordSpec
import org.scalatest.matchers.should.Matchers

trait CoreWireWordSpec extends WireWordSpec with Matchers {
  override val testResourcesFileRoot = "src/test/resources"
}

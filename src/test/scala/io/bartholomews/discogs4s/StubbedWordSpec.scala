package io.bartholomews.discogs4s

import com.softwaremill.diffx.scalatest.DiffMatcher
import io.bartholomews.discogs4s.client.{MockClient, StubbedIO}
import io.bartholomews.discogs4s.server.MockServer
import org.scalatest.{Matchers, WordSpec}

trait StubbedWordSpec extends WordSpec with StubbedIO with MockClient with MockServer with Matchers with DiffMatcher

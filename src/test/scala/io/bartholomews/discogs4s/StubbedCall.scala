package io.bartholomews.discogs4s

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import fsclient.entities.HttpResponse
import fsclient.utils.HttpTypes.IOResponse
import org.scalatest.{Assertion, Inside, Suite}

trait StubbedCall extends Inside {

  self: Suite =>

  def insideResponse[T](stubMapping: => StubMapping,
                        request: IOResponse[T])(pf: PartialFunction[HttpResponse[T], Assertion]): Assertion = {
    stubMapping
    inside[HttpResponse[T], Assertion](request.unsafeRunSync()) {
      pf
    }
  }
}

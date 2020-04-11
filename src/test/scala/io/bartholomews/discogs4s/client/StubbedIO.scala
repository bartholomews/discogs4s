package io.bartholomews.discogs4s.client

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import fsclient.utils.HttpTypes.{HttpResponse, IOResponse}
import org.scalatest.{Assertion, Inside}

trait StubbedIO extends Inside {

  def matchResponse[T](stubMapping: => StubMapping, request: IOResponse[T])(
    pf: PartialFunction[HttpResponse[T], Assertion]
  ): Assertion = {
    stubMapping
    inside(request.unsafeRunSync())(pf)
  }
}

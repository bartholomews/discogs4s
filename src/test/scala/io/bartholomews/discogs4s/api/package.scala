package io.bartholomews.discogs4s

import _root_.com.softwaremill.diffx.{Derived, Diff}
import io.bartholomews.discogs4s.entities.SimpleUser
import org.http4s.Uri

package object api {

  implicit val diffUri: Diff[Uri] = Derived(Diff[String]).contramap[Uri](_.renderString)

  // FIXME: With this it's unreadable
  implicit val simpleUserDiff: Diff[SimpleUser] = Diff.fallback[SimpleUser]
}

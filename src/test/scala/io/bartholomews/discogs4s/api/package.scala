package io.bartholomews.discogs4s

import _root_.com.softwaremill.diffx.{Derived, Diff, DiffResult, DiffResultObject, Identical}
import fsclient.entities.OAuthVersion.Version1.AccessTokenV1
import io.bartholomews.discogs4s.entities.SimpleUser
import org.http4s.Uri
import org.http4s.client.oauth1.{Consumer, Token}

package object api {

  implicit val diffUri: Diff[Uri] = Derived(Diff[String]).contramap[Uri](_.renderString)

  def fromObject[T](value: T, diffResultObject: DiffResultObject): DiffResult =
    diffResultObject.fields.values
      .collectFirst({ case result if !result.isIdentical => diffResultObject })
      .getOrElse(Identical(value))

  // FIXME: With this it's unreadable
  implicit val simpleUserDiff: Diff[SimpleUser] = Diff.fallback[SimpleUser]

  implicit val diffAccessTokenV1: Diff[AccessTokenV1] =
    (left: AccessTokenV1, right: AccessTokenV1, _) => {
      fromObject(
        left,
        DiffResultObject(
          name = "AuthVersion.V1.AccessToken",
          fields = Map(
            "token" -> Diff[Token].apply(left.token, right.token),
            "verifier" -> Diff[Option[String]].apply(left.tokenVerifier, right.tokenVerifier),
            "consumer" -> Diff[Consumer].apply(left.consumer, right.consumer)
          )
        )
      )
    }
}

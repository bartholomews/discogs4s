package io.bartholomews

import com.softwaremill.diffx.{Diff, DiffResult, DiffResultObject, Identical}
import fsclient.entities.OAuthVersion.V1
import org.http4s.client.oauth1.{Consumer, Token}

package object discogs4s {

  def fromObject[T](value: T, diffResultObject: DiffResultObject): DiffResult =
    diffResultObject.fields.values
      .collectFirst({ case result if !result.isIdentical => diffResultObject })
      .getOrElse(Identical(value))

  implicit val diffAccessTokenV1: Diff[V1.AccessToken] =
    (left: V1.AccessToken, right: V1.AccessToken, _) => {
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

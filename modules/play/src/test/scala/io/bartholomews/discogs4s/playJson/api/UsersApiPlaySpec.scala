package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.UsersApiSpec
import io.bartholomews.discogs4s.entities.{SimpleUser, UserIdentity}
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class UsersApiPlaySpec
    extends UsersApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs {
  implicit override def simpleUserDecoder: Reads[SimpleUser] = simpleUserReads
  implicit override def userIdentityDecoder: Reads[UserIdentity] = userIdentityReads
}

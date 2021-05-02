package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.AuthApiSpec
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class AuthApiPlaySpec
    extends AuthApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs

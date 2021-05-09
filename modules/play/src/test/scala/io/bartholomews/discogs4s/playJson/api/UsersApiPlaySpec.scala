package io.bartholomews.discogs4s.playJson.api

import io.bartholomews.discogs4s.api.UsersApiSpec
import io.bartholomews.discogs4s.entities.requests.UpdateUserRequest
import io.bartholomews.discogs4s.entities.{UserContributions, UserIdentity, UserProfile, UserSubmissionResponse}
import io.bartholomews.discogs4s.playJson.{PlayEntityCodecs, PlayServerBehaviours}
import play.api.libs.json.{JsError, JsValue, Reads, Writes}

class UsersApiPlaySpec
    extends UsersApiSpec[Writes, Reads, JsError, JsValue]
    with PlayServerBehaviours
    with PlayEntityCodecs {
  implicit override val userIdentityDecoder: Reads[UserIdentity] = userIdentityReads
  implicit override val userProfileDecoder: Reads[UserProfile]   = userProfileReads
  override implicit val updateUserRequestEncoder: Writes[UpdateUserRequest] = updateUserRequestWrites
  override implicit val userSubmissionResponseDecoder: Reads[UserSubmissionResponse] = userSubmissionResponseCodec
  override implicit val userContributionsDecoder: Reads[UserContributions] = userContributionsCodec
}

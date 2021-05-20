package io.bartholomews.discogs4s.entities

final case class ReleaseRating(username: DiscogsUsername, releaseId: DiscogsReleaseId, rating: Rating)

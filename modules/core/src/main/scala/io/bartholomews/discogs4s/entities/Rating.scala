package io.bartholomews.discogs4s.entities

sealed trait Rating {
  private[discogs4s] def value: Int
}

sealed trait RatingUpdate {
  private[discogs4s] def value: Int
}

object Rating {
  case object NoRating    extends Rating                   { override private[discogs4s] val value: Int = 0 }
  final case object One   extends Rating with RatingUpdate { override private[discogs4s] val value: Int = 1 }
  final case object Two   extends Rating with RatingUpdate { override private[discogs4s] val value: Int = 2 }
  final case object Three extends Rating with RatingUpdate { override private[discogs4s] val value: Int = 3 }
  final case object Four  extends Rating with RatingUpdate { override private[discogs4s] val value: Int = 4 }
  final case object Five  extends Rating with RatingUpdate { override private[discogs4s] val value: Int = 5 }
}

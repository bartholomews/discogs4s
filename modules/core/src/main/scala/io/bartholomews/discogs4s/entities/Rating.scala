package io.bartholomews.discogs4s.entities

sealed trait Rating {
  private[discogs4s] def value: Int
}

sealed trait RatingUpdate

object Rating {
  case object NoRating    extends Rating { override private[discogs4s] val value: Int = 0 }
  final case object One   extends Rating { override private[discogs4s] val value: Int = 1 }
  final case object Two   extends Rating { override private[discogs4s] val value: Int = 2 }
  final case object Three extends Rating { override private[discogs4s] val value: Int = 3 }
  final case object Four  extends Rating { override private[discogs4s] val value: Int = 4 }
  final case object Five  extends Rating { override private[discogs4s] val value: Int = 5 }
}

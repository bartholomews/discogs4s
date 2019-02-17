package client.effect4s.entities

import org.http4s.{Headers, Status}

// TODO see if you can use `Refined` to enforce Status.Ok when entity.isRight and vice-versa
case class HttpResponse[T](headers: Headers, entity: Either[ResponseError, T]) {
  val status: Status = entity.fold(_.status, _ => Status.Ok)
}

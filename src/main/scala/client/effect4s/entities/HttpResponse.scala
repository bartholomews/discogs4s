package client.effect4s.entities

import org.http4s.{Headers, Status}

case class HttpResponse[T](headers: Headers, entity: Either[ResponseError, T]) {
  val status: Status = entity.fold(_.status, _ => Status.Ok)
}

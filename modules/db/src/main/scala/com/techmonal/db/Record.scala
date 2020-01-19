package com.techmonal.db

import java.time.Instant
import java.util.UUID

import io.circe.syntax._
import io.circe.{Encoder, Json}

trait Record {
  def raw: Any
}

object Record {
  type RecordMap = Map[String, Record]

  implicit def stringRecord(t: String): Record = GenRecord(t)

  implicit def intRecord(t: Int): Record = GenRecord(t)

  implicit def booleanRecord(t: Boolean): Record = GenRecord(t)

  implicit def jsonRecord(t: Json): Record = JsonRecord(t)

  implicit def doubleRecord(t: Double): Record = GenRecord(t)

  implicit def longRecord(t: Long): Record = GenRecord(t)

  implicit def instantRecord(t: Instant): Record = GenRecord(t.toEpochMilli)

  implicit def uuidRecord(t: UUID): Record = GenRecord(t)
}

final case class JsonRecord[T](t: T)(implicit tjs: Encoder[T]) extends Record {
  override def raw: Any = t.asJson.noSpaces
}

final case class GenRecord[T](t: T) extends Record {
  override def raw: Any = t
}

package com.techmonal.db.utils

import java.util.UUID

import com.techmonal.common.domains.{Author, Note}
import com.techmonal.db._

import scala.util.Try

object TestData {

  import Schema._

  val uuidString           = UUID.fromString("0fbeb5c6-270c-4c13-ab92-f1dd7ca1ce9e").toString
  val sampleAuthor: Author = Author(uuidString, "Test Author", "test@test.com")
  val sampleNote: Note     = Note(uuidString, "Title", "Test Content", sampleAuthor.id, isActive = true)

  implicit def toNoteSchema(implicit conf: DBConf): Schema[Note] = {

    implicit val tableDetails: TableDetails = TableDetails("id", "notes", conf.keySpace)

    Schema[Note] {
      Seq(
        toColumnDetails[Note, String]("id", _.id),
        toColumnDetails[Note, String]("title", _.title),
        toColumnDetails[Note, String]("content", _.content),
        toColumnDetails[Note, String]("author_id", _.authorId),
        toColumnDetails[Note, Boolean]("is_active", _.isActive)
      )
    }

  }

  implicit object NoteResult extends ResultBuilder[Note] {
    override def rowBuilder(builder: RowBuilder): Try[Note] = {
      import builder._
      import cats.syntax.all._

      (
        getString("id"),
        getString("title"),
        getString("content"),
        getString("author_id"),
        getBool("is_active")
      ).mapN(Note)
    }
  }

  implicit object NoteResultError extends ResultBuilder[Note] {
    override def rowBuilder(builder: RowBuilder): Try[Note] = {
      Try[Note] {
        throw new Exception("Test Exception Occurred!")
      }
    }
  }

}

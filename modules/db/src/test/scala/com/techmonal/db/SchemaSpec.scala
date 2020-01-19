package com.techmonal.db

import java.util.UUID

import com.datastax.driver.core.Statement
import com.techmonal.common.domains.Note
import com.techmonal.db.Schema.toColumnDetails
import com.techmonal.db.utils.TestData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SchemaSpec extends AnyWordSpec with Matchers {

  val schema: Schema[Note] = {

    implicit val tableDetails: TableDetails = TableDetails("id", "notes", "notebook_keyspace")

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

  "Schema" should {

    val mappedValue = schema.toRecordMap(TestData.sampleNote)

    "create basic insert query " in {
      val query: Statement = schema.buildInsertQuery(mappedValue)

      query.toString.shouldBe(
        "INSERT INTO notebook_keyspace.notes (is_active,id,content,author_id,title) VALUES (true,'0fbeb5c6-270c-4c13-ab92-f1dd7ca1ce9e','Test Content','0fbeb5c6-270c-4c13-ab92-f1dd7ca1ce9e','Title') IF NOT EXISTS;"
      )
    }

    "create basic update query " in {
      val query: Statement = schema.buildUpdateQuery(mappedValue)

      query.toString.shouldBe(
        "UPDATE notebook_keyspace.notes SET is_active=true,content='Test Content',author_id='0fbeb5c6-270c-4c13-ab92-f1dd7ca1ce9e',title='Title' WHERE id='0fbeb5c6-270c-4c13-ab92-f1dd7ca1ce9e' IF EXISTS;"
      )
    }

    "create basic select query " in {
      val id               = UUID.randomUUID().toString
      val query: Statement = schema.buildSelectQuery(id)

      query.toString.shouldBe(s"SELECT id,title,content,author_id,is_active FROM notebook_keyspace.notes WHERE id='$id';")
    }

    "create basic select query with IN clause " in {
      val id               = UUID.randomUUID().toString
      val query: Statement = schema.buildSelectQueryIn(Set(id))

      query.toString.shouldBe(s"SELECT id,title,content,author_id,is_active FROM notebook_keyspace.notes WHERE id IN ('$id');")
    }

  }

}

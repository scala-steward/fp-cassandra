package com.techmonal.db

import java.time.Instant
import java.util.UUID

import com.techmonal.common.domains.{Author, Note}
import com.techmonal.db.utils.TwitterMonadicPredef._
import com.techmonal.db.utils.{DatabaseSpec, TestData, TwitterExecutor, TwitterFutures}
import com.twitter.util.Future
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RepositorySpec extends AnyWordSpec with Matchers with DatabaseSpec with TwitterFutures {

  import scala.concurrent.duration._

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(30.seconds, 1.second)

  implicit val executor: DBSession[Future] = TwitterExecutor.futureSession("notebook_keyspace", session)

  "find by id" should {
    import com.techmonal.db.utils.TestData.{toNoteSchema, NoteResult}
    val repo = Repository[Future, String, Note]

    "return one value wrapped in an option" in {
      repo.insert(TestData.sampleNote).futureValue.shouldBe(())
      repo.insert(TestData.sampleNote.copy(isActive = false)).futureValue.shouldBe(())

      repo.find(TestData.sampleNote.id.toString).futureValue.shouldBe(Option(TestData.sampleNote))
    }

    "return none when value is not found" in {
      repo.find(TestData.sampleNote.id + "2XYZ").futureValue.shouldBe(None)
    }
  }

}

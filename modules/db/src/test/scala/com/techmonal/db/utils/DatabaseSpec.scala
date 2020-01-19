package com.techmonal.db.utils

import com.datastax.driver.core.Session
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent._
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._

trait DatabaseSpec extends BeforeAndAfterAll with ScalaFutures {
  this: Suite ⇒

  implicit val patienceConfTimeout: Timeout = Timeout(10.seconds)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra("database.yml", 60.seconds.toMillis)

  lazy val cluster          = EmbeddedCassandraServerHelper.getCluster
  lazy val session: Session = cluster.connect()

  override protected def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("database.yml", 60.seconds.toMillis)
    val session: Session = EmbeddedCassandraServerHelper.getSession
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()

    val dataLoader = new TestDataLoader(session, "notebook_keyspace")
    dataLoader.load("cql/test_tables.cql")

    super.beforeAll()
  }
}

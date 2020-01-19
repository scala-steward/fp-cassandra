package com.techmonal.db.utils

import com.datastax.driver.core.Session
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.CqlOperations.{createKeyspace, use}

class TestDataLoader(session: Session, keyspace: String) {

  def load(files: String*): Unit = {
    createKeyspace(session).accept(keyspace)
    use(session).accept(keyspace)

    for (file <- files) {
      val statements = new ClassPathCQLDataSet(file, keyspace).getCQLStatements
      statements.stream.forEach((s: String) => session.execute(s))
    }
  }

}

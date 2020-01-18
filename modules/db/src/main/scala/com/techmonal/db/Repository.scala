package com.techmonal.db

import cats._
import cats.implicits._
import com.datastax.driver.core._

import scala.util.Success

trait DBConf {
  def keySpace: String
}

trait DBMonadicF[F[_]] {
  def execute(statement: Statement): F[ResultSet]
}

trait DBSession[F[_]] extends DBMonadicF[F] with DBConf

object RepositoryOps {
  import scala.jdk.CollectionConverters._

  def toList(resultSet: ResultSet): List[Row] = resultSet.iterator().asScala.toList

  def toOne(resultSet: ResultSet): Option[Row] =
    if (!resultSet.isExhausted) {
      Some(resultSet.one())
    } else {
      None
    }
}

trait Repository[F[_], IdType, RowType] {
  def find(id: IdType): F[Option[RowType]]
  def find(ids: Set[IdType]): F[List[RowType]]
  def findAll(id: IdType): F[List[RowType]]
  def insert(value: RowType): F[Unit]
  def insert(value: RowType, id: IdType): F[RowType]
  def update(value: RowType): F[Unit]
}

object Repository {
  import RepositoryOps._

  def apply[F[_]: MonadError[*[_], Throwable]: DBMonadicF, IdType, RowType: Schema: ResultBuilder]: Repository[F, IdType, RowType] =
    new Repository[F, IdType, RowType] {

      private val schema       = implicitly[Schema[RowType]]
      private val context      = implicitly[DBMonadicF[F]]
      private val typeResolver = implicitly[ResultBuilder[RowType]]
      private val monadError   = implicitly[MonadError[F, Throwable]]

      private def resolve(row: Row): F[RowType] = monadError.fromTry(Success(row).flatMap(r => typeResolver.resultBuilder(r)))

      private def resolveK[M[_]: MonoidK: Monad: Foldable](rows: M[Row]): F[M[RowType]] = {
        import monadError._
        val zero = point(implicitly[MonoidK[M]].empty[RowType])
        rows
          .map(resolve)
          .foldLeft(zero)((acc, elem) =>
            flatMap(acc) { row: M[RowType] =>
              map(elem) { e: RowType =>
                row.combineK(implicitly[Monad[M]].pure(e))
              }
            }
          )
      }

      def find(id: IdType): F[Option[RowType]] = {
        val query: Statement = schema.buildSelectQuery(id)
        context.execute(query).map(toOne).flatMap(resolveK[Option])
      }

      def find(ids: Set[IdType]): F[List[RowType]] = {
        val query: Statement = schema.buildSelectQueryIn(ids)
        context.execute(query).map(toList).flatMap(resolveK[List])
      }

      def findAll(id: IdType): F[List[RowType]] = {
        val query: Statement = schema.buildSelectQuery(id)
        context.execute(query).map(toList).flatMap(resolveK[List])
      }

      def insert(value: RowType): F[Unit] = {
        val mappedValue      = schema.toRecordMap(value)
        val query: Statement = schema.buildInsertQuery(mappedValue)
        context.execute(query).map(_ => ())
      }

      def insert(value: RowType, id: IdType): F[RowType] = {
        val mappedValue      = schema.toRecordMap(value)
        val query: Statement = schema.buildInsertQuery(mappedValue)

        context.execute(query).flatMap { resultSet =>
          if (resultSet.wasApplied) {
            value.pure[F]
          } else {
            find(id).map(_.get)
          }
        }
      }

      def update(value: RowType): F[Unit] = {
        val mappedValue      = schema.toRecordMap(value)
        val query: Statement = schema.buildUpdateQuery(mappedValue)
        context.execute(query).map(_ => ())
      }
    }
}

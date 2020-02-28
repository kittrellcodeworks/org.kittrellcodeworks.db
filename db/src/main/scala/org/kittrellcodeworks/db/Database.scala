package org.kittrellcodeworks.db

import scala.collection.IterableOnce

abstract class Database[Key, R[X] <: Record[X, Key]](keyField: String,
                                                     discriminatorField: String)
  extends DatabaseHelpers[Key, R] {

  /**
   * A Database defines its own transaction monad
   */
  type Transaction[_] <: TransactionLike[_, Transaction]

  def insert[T: R](kms: IterableOnce[T]): Transaction[Unit]

  def delete(query: Query): Transaction[Unit]

  def exists(query: Query): Transaction[Boolean]

  def count(query: Query): Transaction[Int]

  def find[T: R](query: Query): Transaction[Seq[T]]

  def findOne[T: R](query: Query): Transaction[Option[T]]

  // --- pre-defined convenience methods ---

  final def insert[T: R](km: T): Transaction[Unit] = insert(Seq(km))

  final def replaceAll[T: R](replacement: T): Transaction[Unit] =
    delete(implicitly[R[T]].discriminatorQuery).flatMap(_ => insert(replacement))

}

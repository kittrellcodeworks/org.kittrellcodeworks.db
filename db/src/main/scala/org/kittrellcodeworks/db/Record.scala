package org.kittrellcodeworks.db

import scala.reflect.ClassTag

/**
 * A super-class of an implementation-provided Algebraic Data Type that determines what values a Database
 * can accept as parameters and return as results.
 *
 * Database implementations will not provide Record ADTs. These should be provided by client applications.
 */
abstract class Record[T, Key](implicit val ct: ClassTag[T]) {

  /**
   * get the key value of a record of type T
   */
  def getKey(record: T): Key

  /**
   * a query part for limiting query results to just type T
   */
  val discriminatorQuery: QueryPart = Empty

}

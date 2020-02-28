package org.kittrellcodeworks.db

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait DatabaseHelpers[Key, R[X] <: Record[X, Key]] {

  /**
   * Allows the underlying databaseto determine the key field value of a record.
   */
  protected implicit def getKey[T](record: T)(implicit r: R[T]): Key = r getKey record

  /**
   * Allows database implementations to use type parameters in pattern matching.
   */
  protected implicit def recordClassTag[T](implicit r: R[T]): ClassTag[T] = r.ct

  type KeyMagnet[T] = T => Key

  object KeyMagnet {
    implicit val keyMagnet: KeyMagnet[Key] = identity[Key]

    implicit def recordMagnet[T](implicit r: R[T]): KeyMagnet[T] = r.getKey
  }

}

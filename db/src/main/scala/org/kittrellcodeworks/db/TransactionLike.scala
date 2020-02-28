package org.kittrellcodeworks.db

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 * Super-class of the Transaction monad. Database implementations will provide their own concrete
 * Transaction subclasses that implement the `begin`, `commit`, `rollback` and `copy` methods.
 */
abstract class TransactionLike[+T, Repr[X] <: TransactionLike[T, Repr]](_stage: => Future[T])
                                                                      (implicit ec: ExecutionContext) {

  // allows other instances to access the pass-by-reference parameter and keep its execution lazy.
  final private def stage: Future[T] = _stage

  /**
   * Begins a transaction. Called before the execution of the transaction.
   */
  protected def begin(): Future[Unit]

  /**
   * Commits a transaction. Called after the successful execution of the transaction.
   */
  protected def commit(): Future[Unit]

  /**
   * Rolls back a transaction. Called after the failed execution of the transaction.
   */
  protected def rollback(): Future[Unit]

  /**
   * Creates a copy of this transaction with a new behavior stage.
   * This allows the composition of transactions while maintaining lazy execution.
   */
  // maybe we'll need to account for rollback stages? ignore for now.
  protected def copy[S](newStage: => Future[S]): Repr[S]

  /**
   * Executes the transaction, returning the result.
   * Every call to `execute` will replay the entire transaction.
   */
  final def execute(): Future[T] = {
    val p = Promise[T]()
    stage onComplete {
      case Success(result) => p completeWith commit().map(_ => result)
      case Failure(error) => p completeWith rollback().map(_ => throw error)
    }
    p.future
  }

  final def map[S](f: T => S): Repr[S] = copy(stage.map(f))

  final def flatMap[S](f: T => Repr[S]): Repr[S] = copy(stage.flatMap(f(_).stage.asInstanceOf[Future[S]]))

  final def withFilter[S >: T](predicate: S => Boolean): Repr[S] = copy(stage withFilter predicate)

  final def foreach(f: T => Unit): Unit = execute foreach f

}

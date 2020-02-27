package org.kittrellcodeworks.db

import scala.language.implicitConversions
import scala.reflect.ClassTag

sealed trait QueryPart {
  def simplify: QueryPart = this

  def unary_! : QueryPart = Not(this)

  def getField[T: ClassTag](field: String): Option[T] = None

  def size: Int = 1

  protected def innerSplit(clauseLimit: Int): Seq[QueryPart] = Seq(this)

  final def split(clauseLimit: Int): Seq[QueryPart] = simplify.innerSplit(clauseLimit)
}

object QueryPart {
  implicit def queryOpt2query[T <: QueryPart](qo: Option[T]): QueryPart = qo getOrElse Empty
}

case class OrQuery(parts: Seq[QueryPart]) extends QueryPart {
  override def simplify: QueryPart = {
    val newParts = parts.map(_.simplify).foldLeft(List.empty[QueryPart]) {
      case (acc, Empty) ⇒ acc
      case (acc, OrQuery(more)) ⇒ more.foldLeft(acc)(_.::(_))
      case (acc, p) ⇒ p :: acc
    }
    if (newParts.nonEmpty) OrQuery(newParts) else Empty
  }

  override def size: Int = parts.map(_.size).sum

  override protected def innerSplit(clauseLimit: Int): Seq[QueryPart] =
    parts.grouped(clauseLimit).map(OrQuery).toSeq

}

case class AndQuery(parts: Seq[QueryPart]) extends QueryPart {
  override def simplify: QueryPart = {
    val newParts = parts.map(_.simplify).foldLeft(List.empty[QueryPart]) {
      case (acc, Empty) ⇒ acc
      case (acc, AndQuery(more)) ⇒ more.foldLeft(acc)(_.::(_))
      case (acc, p) ⇒ p :: acc
    }
    if (newParts.nonEmpty) AndQuery(newParts) else Empty
  }

  override def getField[T: ClassTag](field: String): Option[T] =
    parts.toStream.flatMap(_.getField[T](field)).headOption

  override def size: Int = parts.map(_.size).sum

  override protected def innerSplit(clauseLimit: Int): Seq[QueryPart] = {
    // NOTE - this destroys attempts to short-circuit comparisons
    val (others0, maxSize, max) = parts.foldLeft((List.empty[QueryPart], 0, Empty:QueryPart)) {
      case ((acc, maxSize, maxPart), or: OrQuery) if or.size > maxSize ⇒ (maxPart :: acc, or.size, or)
      case ((acc, maxSize, maxPart), any: IsAnyOf) if any.size > maxSize ⇒ (maxPart :: acc, any.size, any)
      case ((acc, maxSize, maxPart), part) ⇒ (part :: acc, maxSize, maxPart)
    }
    val others = others0.filter(_ ne Empty)
    val split = max match {
      case p@(_:OrQuery | _:IsAnyOf) if maxSize <= clauseLimit ⇒ p.split(clauseLimit)
      case p ⇒ Seq(p)
    }
    split map {
      case Empty ⇒ this
      case p ⇒ AndQuery(p :: others)
    }
  }
}

case class IsAnyOf(field: String, values: Iterable[Any]) extends QueryPart {
  override def simplify: QueryPart = {
    if (values.isEmpty) Empty else IsAnyOf(field, values.toSet)
  }

  override def size: Int = values.size

  override protected def innerSplit(clauseLimit: Int): Seq[QueryPart] = {
    values.grouped(clauseLimit).toSeq.map(vs ⇒ copy(values = vs))
  }
}

case class Contains(field: String, value: Any) extends QueryPart

case class FieldValue(field: String, value: Any) extends QueryPart {
  override def getField[T: ClassTag](field: String): Option[T] =
    if (this.field == field) implicitly[ClassTag[T]] unapply value else None
}

case class Range(field: String, lower: Option[Any], upper: Option[Any]) extends QueryPart {
  override def simplify: QueryPart = {
    if (lower.isEmpty && upper.isEmpty) Empty else this
  }
}

case class Not(qp: QueryPart) extends QueryPart {
  override def simplify: QueryPart = {
    val simp = qp.simplify
    if (simp ne Empty) Not(simp) else Empty
  }

  override def unary_! : QueryPart = qp
}

case class Exists(field: String) extends QueryPart

case object Empty extends QueryPart {
  override def size: Int = 0
}

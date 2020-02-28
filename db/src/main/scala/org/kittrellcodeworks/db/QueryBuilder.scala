package org.kittrellcodeworks.db

import scala.language.implicitConversions

sealed trait QueryBuilder {
  def result: QueryPart
}

sealed trait AddableQueryBuilder[Next <: QueryBuilder] extends QueryBuilder {
  private[db] def add(part: QueryPart): Next
}

object QueryBuilder {

  def apply(field: String): FieldQueryBuilder[SingleQueryBuilder] = new FieldQueryBuilder(EmptyBuilder, field)

  def apply(part: QueryPart): SingleQueryBuilder = new SingleQueryBuilder(part)

  implicit def toResult[B <: QueryBuilder](b: B): QueryPart = b.result

  object EmptyBuilder extends AddableQueryBuilder[SingleQueryBuilder] {
    private[db] def add(part: QueryPart): SingleQueryBuilder = new SingleQueryBuilder(part)

    def result: QueryPart = Empty
  }

  class SingleQueryBuilder(part: QueryPart) extends QueryBuilder {
    def result: QueryPart = part

    def and(field: String): FieldQueryBuilder[AndQueryBuilder] = new AndQueryBuilder and part and field

    def and(other: QueryPart): AndQueryBuilder = new AndQueryBuilder add part add other

    def or(field: String): FieldQueryBuilder[OrQueryBuilder] = new OrQueryBuilder or part or field

    def or(other: QueryPart): OrQueryBuilder = new OrQueryBuilder add part add other
  }

  class AndQueryBuilder extends AddableQueryBuilder[AndQueryBuilder] {
    private var parts = List.empty[QueryPart]

    private[db] def add(part: QueryPart): this.type = {
      if (part != Empty) parts ::= part
      this
    }

    def and(field: String): FieldQueryBuilder[AndQueryBuilder] = new FieldQueryBuilder(this, field)

    def and(other: QueryPart): this.type = add(other)

    def result: QueryPart = if (parts.nonEmpty) AndQuery(parts.reverse) else Empty
  }

  class OrQueryBuilder extends AddableQueryBuilder[OrQueryBuilder] {
    private var parts = List.empty[QueryPart]

    private[db] def add(part: QueryPart): this.type = {
      if (part != Empty) parts ::= part
      this
    }

    def or(field: String): FieldQueryBuilder[OrQueryBuilder] = new FieldQueryBuilder(this, field)

    def or(other: QueryPart): this.type = add(other)

    def result: QueryPart = if (parts.nonEmpty) OrQuery(parts.reverse) else Empty
  }

}



package org.kittrellcodeworks.db

import scala.language.implicitConversions

trait QuerySupport {

  implicit def query(field: String): FieldQueryBuilder[QueryBuilder.SingleQueryBuilder] = QueryBuilder(field)

  implicit def query(part: QueryPart): QueryBuilder.SingleQueryBuilder = QueryBuilder(part)

  implicit def query(b: QueryBuilder): QueryBuilder.SingleQueryBuilder = QueryBuilder(b.result)

}

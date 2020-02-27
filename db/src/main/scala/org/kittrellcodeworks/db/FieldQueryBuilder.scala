package org.kittrellcodeworks.db

class FieldQueryBuilder[Out <: QueryBuilder](b: AddableQueryBuilder[Out], field: String) {
  def is(v: Any): Out = is(Option(v))
  def is(v: Option[Any]): Out = b.add(v.map(FieldValue(field, _)) getOrElse Empty)
  def isNot(v: Any): Out = isNot(Option(v))
  def isNot(v: Option[Any]): Out = b.add(v.map(!FieldValue(field, _)) getOrElse Empty)
  def isAnyOf(vs: Iterable[Any]): Out = b.add(if (vs.nonEmpty) IsAnyOf(field, vs) else Empty)
  def isNoneOf(vs: Iterable[Any]): Out = b.add(if (vs.nonEmpty) !IsAnyOf(field, vs) else Empty)
  def isInRange(lower: Any, upper: Any): Out = b.add(Range(field, Option(lower), Option(upper)))
  def isInRange(lower: Option[Any], upper: Option[Any]): Out = b.add(Range(field, lower, upper))
  def contains(value: Any): Out = contains(Option(value))
  def contains(value: Option[Any]): Out = b.add(value.map(Contains(field, _)) getOrElse Empty)
  def exists(): Out = b.add(Exists(field))
  def doesNotExist(): Out = b.add(!Exists(field))
}

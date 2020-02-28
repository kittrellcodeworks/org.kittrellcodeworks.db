package org.kittrellcodeworks.db

trait DatabaseFactory {

  def createDatabase[Key, R[X] <: Record[X, Key]](): Database[Key, R]

}

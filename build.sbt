import Dependencies._

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.kittrellcodeworks"
ThisBuild / organizationName := "Kittrell Codeworks"

lazy val root = (project in file("db"))
  .settings(
    name := "db",
    libraryDependencies += scalaTest % Test
  )

lazy val solr = (project in file("db-solr"))
  .settings(
    name := "db-solr",
    libraryDependencies += scalaTest % Test
  )
  .dependsOn(root)

lazy val mongo = (project in file("db-mongo"))
  .settings(
    name := "db-mongo",
    libraryDependencies += scalaTest % Test
  )
  .dependsOn(root)

lazy val mem = (project in file("db-mem"))
  .settings(
    name := "db-mem",
    libraryDependencies += scalaTest % Test
  )
  .dependsOn(root)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

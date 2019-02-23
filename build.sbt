name := "claimant"

scalaVersion := "2.12.6"

scalacOptions ++=
  "-deprecation" ::
  "-encoding" :: "UTF-8" ::
  "-feature" ::
  "-Xfatal-warnings" ::
  "-Xfuture" ::
  "-Yno-adapted-args" ::
  "-Ypartial-unification" ::
  Nil

libraryDependencies ++=
  "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
  "org.scalacheck" %% "scalacheck" % "1.14.0" ::
  "org.typelevel" %% "cats-core" % "1.6.0"  % "test" ::
  Nil

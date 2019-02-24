import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val claimantSettings = Seq(
  organization := "org.spire-math",
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.6"),
  libraryDependencies ++=
    "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
    "org.scalacheck" %%% "scalacheck" % "1.14.0" ::
    "org.typelevel" %%% "cats-core" % "1.6.0"  % "test" ::
    Nil,
  scalacOptions ++=
    "-deprecation" ::
    "-encoding" :: "UTF-8" ::
    "-feature" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    "-language:experimental.macros" ::
    "-unchecked" ::
    "-Xfatal-warnings" ::
    "-Xlint" ::
    "-Yno-adapted-args" ::
    "-Ywarn-dead-code" ::
    "-Ywarn-numeric-widen" ::
    "-Ywarn-value-discard" ::
    "-Xfuture" ::
    Nil,
  // HACK: without these lines, the console is basically unusable,
  // since all imports are reported as being unused (and then become
  // fatal errors).
  scalacOptions in (Compile, console) ~= { _.filterNot("-Xlint" == _) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  // release stuff
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/non/claimant"),
      "scm:git:git@github.com:non/claimant.git")),
  homepage := Some(url("https://github.com/non/claimant/")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer("non", "Erik Osheim", "erik@osheim.org", url("http://github.com/non/"))))

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project
  .in(file("."))
  .settings(name := "root")
  .settings(claimantSettings: _*)
  .settings(noPublish: _*)
  .aggregate(coreJVM, coreJS)
  .dependsOn(coreJVM, coreJS)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(name := "claimant")
  .settings(claimantSettings: _*)
  .jsSettings(
    scalaJSStage in Global := FastOptStage,
    parallelExecution := false,
    coverageEnabled := false,
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv())

lazy val coreJVM = core.jvm

lazy val coreJS = core.js


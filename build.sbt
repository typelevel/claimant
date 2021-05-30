import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / scalaVersion := "2.12.14"
ThisBuild / crossScalaVersions := Seq("2.12.14", "2.13.6")

lazy val claimantSettings = Seq(
  organization := "org.typelevel",
  libraryDependencies ++=
    "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
      "org.scalacheck" %%% "scalacheck" % "1.15.4" ::
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
      //"-Xfatal-warnings" :: // kind of brutal in 2.13
      "-Xlint" ::
      "-Ywarn-dead-code" ::
      "-Ywarn-numeric-widen" ::
      "-Ywarn-value-discard" ::
      Nil,
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Seq("-Xfuture")
      case _ =>
        Nil
    }
  },
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
  credentials += Credentials(
    Option(System.getProperty("build.publish.credentials"))
      .map(new File(_))
      .getOrElse(Path.userHome / ".ivy2" / ".credentials")
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("Releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  scmInfo := Some(ScmInfo(url("https://github.com/non/claimant"), "scm:git:git@github.com:non/claimant.git")),
  homepage := Some(url("https://github.com/non/claimant/")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(Developer("non", "Erik Osheim", "erik@osheim.org", url("http://github.com/non/")))
)

lazy val noPublish = Seq(publish := {}, publishLocal := {}, publishArtifact := false)

lazy val root = project
  .in(file("."))
  .settings(name := "root")
  .settings(claimantSettings: _*)
  .settings(noPublish: _*)
  .aggregate(mcJVM, mcJS, mcNative, coreJVM, coreJS, coreNative)
  .dependsOn(mcJVM, mcJS, mcNative, coreJVM, coreJS, coreNative)

lazy val mc = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("mc"))
  .settings(name := "claimant-mc")
  .settings(claimantSettings: _*)
  .jsSettings(scalaJSStage in Global := FastOptStage,
              parallelExecution := false,
              coverageEnabled := false,
              jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()
  )

lazy val mcJVM = mc.jvm

lazy val mcJS = mc.js

lazy val mcNative = mc.native

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .dependsOn(mc)
  .settings(name := "claimant")
  .settings(claimantSettings: _*)
  .settings(sourceGenerators in Compile += (sourceManaged in Compile).map(Boilerplate.gen).taskValue)
  .jsSettings(scalaJSStage in Global := FastOptStage,
              parallelExecution := false,
              coverageEnabled := false,
              jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()
  )

lazy val coreJVM = core.jvm

lazy val coreJS = core.js

lazy val coreNative = core.native

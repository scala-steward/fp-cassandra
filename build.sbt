import sbtrelease.ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._

// -------------------------------------------------------------------------------------------------------------------
// Root Project
// -------------------------------------------------------------------------------------------------------------------
lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.techmonal",
      scalaVersion := "2.13.1",
      scalastyleFailOnError := true,
      scalastyleFailOnWarning := false,
      scalafmtOnCompile := true
    )),
    name := "fp-cassandra"
  )
  .aggregate(common, db, web)
  .dependsOn(common, db, web)

// -------------------------------------------------------------------------------------------------------------------
// Common Module
// -------------------------------------------------------------------------------------------------------------------
lazy val common = project.in(file("modules/common"))
  .settings(name := "common")
  .settings(libraryDependencies ++= commonLibraryDependencies)

// -------------------------------------------------------------------------------------------------------------------
// Web Module
// -------------------------------------------------------------------------------------------------------------------
lazy val web = project.in(file("modules/web"))
  .settings(name := "web")
  .aggregate(common).dependsOn(common)
  .settings(libraryDependencies ++= webLibraryDependencies)

// -------------------------------------------------------------------------------------------------------------------
// DB Module
// -------------------------------------------------------------------------------------------------------------------
lazy val db = project.in(file("modules/db"))
  .settings(name := "db")
  .aggregate(common).dependsOn(common)
  .settings(libraryDependencies ++= dbLibraryDependencies)

lazy val commonLibraryDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

lazy val webLibraryDependencies = Seq()

lazy val dbLibraryDependencies = Seq()

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-language:higherKinds",
  "-language:postfixOps",
  "-deprecation"
)

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Bintray ".at("https://dl.bintray.com/projectseptemberinc/maven")
)

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  // publishing locally in the process
  releaseStepCommandAndRemaining("+publishLocal"),
  releaseStepCommandAndRemaining("+clean"),
  releaseStepCommandAndRemaining("+test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

addCommandAlias("validate", "; clean; compile; test;")

import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import scoverage.ScoverageSbtPlugin
import org.sbtidea.SbtIdeaPlugin._

val Organization = "jp.seraphr"

val Version = "0.0.1"

val ScalaVersion = "2.10.4"

val COMMON_SETTINGS = Defaults.defaultSettings ++ Seq(
  organization := Organization,
  version := Version,
  scalaVersion := ScalaVersion,
  EclipseKeys.withSource := true,
  EclipseKeys.withJavadoc := true,
  EclipseKeys.eclipseOutput := Some(".eclipseTarget"),
  ideaExcludeFolders := Seq(".idea", ".idea_modules"),
  testOptions in ScoverageTest := Seq(
    Tests.Argument("-oS", "-u", "target/junit"),
    Tests.Argument("-l", "org.scalatest.tags.Slow")
  ),
  testOptions in Test := Seq(
    Tests.Argument("-oS", "-u", "target/junit")
  ),
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits", "-diagrams"),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-deprecation", "-Xlint"),
  javacOptions ++= Seq("-encoding", "UTF-8"),
  incOptions := incOptions.value.withNameHashing(true)
) ++ ScoverageSbtPlugin.instrumentSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

val COMMON_DEPENDENCIES = Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "test",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
)

lazy val root = Project(
  id = "root",
  base = file(".")
) aggregate(sandbox)

lazy val sandbox = Project(
  id = "sandbox",
  base = file("sandbox"),
  settings = COMMON_SETTINGS ++ Seq(
    name := "sandbox",
    resolvers += "apache.snapshots" at "http://repository.apache.org/snapshots/",
    libraryDependencies ++= COMMON_DEPENDENCIES ++ Seq(
      "org.apache.pdfbox" % "pdfbox" % "1.8.6",
      "org.apache.commons" % "commons-imaging" % "1.0-SNAPSHOT",
      "com.wapmx.native" % "mx-native-loader" % "1.8")
  )
)

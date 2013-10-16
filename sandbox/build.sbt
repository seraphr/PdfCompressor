name := "pdf-compressor-sandbox"

version := "0.0.1"

scalaVersion := "2.10.2"

organization := "jp.seraphr"

libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.2"

libraryDependencies += "org.apache.commons" % "commons-imaging" % "1.0-SNAPSHOT"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"

EclipseKeys.withSource := true

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

resolvers += "apache.snapshots" at "http://repository.apache.org/snapshots/"
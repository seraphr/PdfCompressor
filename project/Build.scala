import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object PdfCompressorBuild extends Build {
  lazy val root = Project(
    id = "root",
    base = file(".")
  ) aggregate(sandbox)

  lazy val sandbox = Project(
    id = "sandbox",
    base = file("sandbox")
  )
}

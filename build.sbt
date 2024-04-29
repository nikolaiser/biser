val scala3Version = "3.3.3"

val BesomVersion = "0.3.1"
val BesomProxmoxveVersion = "6.3.1-core.0.3"
val BesomCommandVersion = "0.10.0-core.0.3"

inThisBuild(
  List(
    organization := "io.github.nikolaiser",
    homepage := Some(url("https://github.com/nikolaiser/biser")),
    // Alternatively License.Apache2 see https://github.com/sbt/librarymanagement/blob/develop/core/src/main/scala/sbt/librarymanagement/License.scala
    licenses := List(
      "MIT" -> url("https://opensource.org/licenses/MIT")
    ),
    developers := List(
      Developer(
        "nikolaiser",
        "Nikolai Sergeev",
        "mail@nikolaiser.com",
        url("https://github.com/nikolaiser")
      )
    )
  )
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

lazy val root = project
  .in(file("."))
  .settings(
    name := "biser",
    organization := "nikolaiser",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.virtuslab" %% "besom-core" % BesomVersion,
      "org.virtuslab" %% "besom-proxmoxve" % BesomProxmoxveVersion,
      "org.virtuslab" %% "besom-command" % BesomCommandVersion
    )
  )

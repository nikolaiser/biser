val scala3Version = "3.3.3"

val BesomVersion = "0.3.1"
val BesomProxmoxveVersion = "6.3.1-core.0.3"
val BesomCommandVersion = "0.10.0-core.0.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "biser",
    organization := "nikolaiser",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.virtuslab" %% "besom-core" % BesomVersion,
      "org.virtuslab" %% "besom-proxmoxve" % BesomProxmoxveVersion,
      "org.virtuslab" %% "besom-command" % BesomCommandVersion
    )
  )

val scala3Version = "3.4.2"

val BesomVersion          = "0.3.2"
val BesomProxmoxveVersion = "6.3.1-core.0.3"
val BesomCommandVersion   = "0.10.0-core.0.3"
val IzumiVersion          = "1.2.9"
val ZioVersion            = "2.1.3"
val ZioInteropCatsVersion = "23.1.0.2"

inThisBuild(
  List(
    organization := "com.nikolaiser",
    homepage     := Some(url("https://github.com/nikolaiser/biser")),
    // Alternatively License.Apache2 see https://github.com/sbt/librarymanagement/blob/develop/core/src/main/scala/sbt/librarymanagement/License.scala
    licenses     := List(
      "MIT" -> url("https://opensource.org/licenses/MIT")
    ),
    developers   := List(
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
sonatypeRepository                 := "https://s01.oss.sonatype.org/service/local"

ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .settings(
    name         := "biser",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.virtuslab"  %% "besom-core"       % BesomVersion,
      "org.virtuslab"  %% "besom-zio"        % BesomVersion,
      "org.virtuslab"  %% "besom-proxmoxve"  % BesomProxmoxveVersion,
      "org.virtuslab"  %% "besom-command"    % BesomCommandVersion,
      "io.7mind.izumi" %% "distage-core"     % IzumiVersion,
      "dev.zio"        %% "zio"              % ZioVersion,
      "dev.zio"        %% "zio-interop-cats" % ZioInteropCatsVersion
    )
  )

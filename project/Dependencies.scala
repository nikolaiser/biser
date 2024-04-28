import sbt.*

object Dependencies {
  lazy val all = Seq(
    "org.virtuslab" %% "besom-core" % DependencyVersions.Besom,
    "org.virtuslab" %% "besom-proxmoxve" % DependencyVersions.BesomProxmoxve
  )
}

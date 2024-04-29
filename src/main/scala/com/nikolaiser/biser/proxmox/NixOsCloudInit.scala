package com.nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve
import besom.internal.RegistersOutputs
import com.nikolaiser.biser.nix.FlakeBuild

case class NixOsCloudInit(
    file: Output[proxmoxve.storage.File]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object NixOsCloudInit:

  def apply(using Context)(
      nodeName: String,
      flake: String,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[NixOsCloudInit] =
    component(
      s"""biser-nixos-cloud-init-$nodeName""",
      "biser:proxmox:NixOsCloudInit",
      options
    ) {
      val imageBuild = FlakeBuild(flake)

      val cloudInitImageFile = proxmoxve.storage.File(
        s"biser-nixos-cloud-init-$nodeName",
        proxmoxve.storage.FileArgs(
          contentType = "snippets",
          datastoreId = "local",
          nodeName = nodeName,
          sourceFile = proxmoxve.storage.inputs.FileSourceFileArgs(
            path = imageBuild.flatMap(_.path).map(base => s"$base/nixos.img")
          )
        )
      )
      NixOsCloudInit(cloudInitImageFile)
    }

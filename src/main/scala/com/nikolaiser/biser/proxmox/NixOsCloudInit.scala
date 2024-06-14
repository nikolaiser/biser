package com.nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve
import besom.internal.RegistersOutputs
import com.nikolaiser.biser.nix.FlakeBuild

case class NixOsCloudInit private (
    file: Output[proxmoxve.storage.File]
)(using ComponentBase)
    extends ComponentResource derives RegistersOutputs

object NixOsCloudInit:

  case class Params(
      flake: Input[String],
      nodeName: Input[String]
  )

  def apply(using Context)(
      name: NonEmptyString,
      params: Params,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[NixOsCloudInit] =
    component(
      name,
      "biser:proxmox:NixOsCloudInit",
      options
    ) {
      val imageBuild = FlakeBuild(
        s"$name-image-build",
        FlakeBuild.Params(params.flake)
      )

      val cloudInitImageFile = proxmoxve.storage.File(
        s"$name-image-upload",
        proxmoxve.storage.FileArgs(
          contentType = "snippets",
          datastoreId = "local",
          nodeName = params.nodeName,
          sourceFile = proxmoxve.storage.inputs.FileSourceFileArgs(
            path = p"${imageBuild.flatMap(_.path)}/nixos.img"
          )
        )
      )

      NixOsCloudInit(cloudInitImageFile)
    }

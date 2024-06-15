package com.nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve.storage.File
import besom.api.proxmoxve.storage.FileArgs
import besom.api.proxmoxve.storage.inputs.FileSourceFileArgs
import besom.api.proxmoxve.Provider
import com.nikolaiser.biser.nix.Image
import izumi.distage.model.definition.With

trait NodeImage:
  def file: Output[File]

object NodeImage:

  type Factory = String => NodeImage @With[NodeImage.Impl]

  case class Impl(
      nodeName: String,
      baseImage: Image,
      provider: Provider
  )(using
      Context
  ) extends NodeImage:
    val file: Output[File] =
      File(
        s"$nodeName-image-upload",
        FileArgs(
          contentType = "snippets",
          datastoreId = "local",
          nodeName = nodeName,
          sourceFile = FileSourceFileArgs(path = baseImage.path)
        ),
        opts(provider = provider)
      )

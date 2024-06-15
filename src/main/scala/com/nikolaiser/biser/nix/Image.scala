package com.nikolaiser.biser.nix

import besom.*
import izumi.distage.model.definition.Id
import besom.api.command.local.Command
import besom.api.command.local.CommandArgs
import com.nikolaiser.biser.common.SshConfig

trait Image:

  /** @return
    *   Path to the image file
    */
  def path: Output[String]

object Image:
  case class Impl(flake: String @Id("base-image-flake"), sshConfig: SshConfig)(using Context) extends Image:

    private val cmd =
      s"""purga --arg sshKey='${sshConfig.publicKey}' -- nix build $flake --no-link --refresh --json 2> /dev/null | jq '.[0].outputs.out' --raw-output"""

    val path: Output[String] =
      Command(s"$flake-base-image-build", CommandArgs(create = cmd)).stdout.map(_ + "/nixos.img")

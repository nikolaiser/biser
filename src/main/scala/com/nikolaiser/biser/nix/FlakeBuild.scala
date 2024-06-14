package com.nikolaiser.biser.nix

import besom.*
import besom.api.command
import besom.internal.RegistersOutputs

case class FlakeBuild private (
    path: Output[String]
)(using ComponentBase)
    extends ComponentResource derives RegistersOutputs

object FlakeBuild:

  case class Params(
      flake: Input[String]
  )

  def apply(using Context)(
      name: NonEmptyString,
      params: Params,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[FlakeBuild] =
    component(
      name,
      "biser:nix:FlakeBuild",
      options
    ) {
      val flakeBuildCommand = command.local.Command(
        s"$name-build-command",
        command.local.CommandArgs(
          create = p"""nix build ${params.flake} --no-link --json 2> /dev/null | jq '.[0].outputs.out' --raw-output"""
        )
      )

      FlakeBuild(flakeBuildCommand.stdout)
    }

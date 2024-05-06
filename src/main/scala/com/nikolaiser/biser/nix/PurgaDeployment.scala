package com.nikolaiser.biser.nix

import besom.*
import besom.api.command
import besom.internal.RegistersOutputs
import besom.json.JsonWriter
import scala.concurrent.Future
import Pulumi.given_ExecutionContext

case class PurgaDeployment private (
    config: Output[String]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object PurgaDeployment:

  case class Params[A](
      flake: Input[String],
      flakeInput: Input[String] = "purgaArgs",
      config: Input[A],
      targetHost: Input[String] // including usrname@
  )

  def apply[A: JsonWriter](using Context)(
      name: NonEmptyString,
      params: Params[A],
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[PurgaDeployment] =
    component(
      name,
      "biser:nix:PurgaDeployment",
      options
    ) {
      val jsonConfig = params.config.asOutput().map { conf => summon[JsonWriter[A]].write(conf).toString }

      val deployment = for {

        config <- jsonConfig

        _ <- command.local
               .Command(
                 s"$name-deploy",
                 command.local.CommandArgs(
                   create =
                     p"""f=$$(mktemp); echo '$config' > $$f ; nixos-rebuild switch < /dev/null --use-remote-sudo --target-host ${params.targetHost} --show-trace --flake "${params.flake}";rm-rf $$f""",
                   environment = Map(
                     "NIX_SSHOPTS" -> "-t -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
                   )
                 )
               )
               .stdout

      } yield config

      PurgaDeployment(deployment)
    }

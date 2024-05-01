package com.nikolaiser.biser.nix

import besom.*
import besom.api.command
import besom.internal.RegistersOutputs
import besom.json.JsonWriter
import scala.concurrent.Future
import Pulumi.given_ExecutionContext

case class ConfigurableFlakeDeployment private (
    flakeDir: Output[String]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object ConfigurableFlakeDeployment:

  case class Params[A](
      flakeRepository: Input[String],
      flakeOutput: Input[String],
      config: Input[A],
      targetHost: Input[String], // including usrname@
      configLocation: Input[String] = "./config.json"
  )

  def apply[A: JsonWriter](using Context)(
      name: NonEmptyString,
      params: Params[A],
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[ConfigurableFlakeDeployment] =
    component(
      name,
      "biser:nix:ConfigurableFlakeDeployment",
      options
    ) {
      val jsonConfig = params.config.asOutput().map(conf => summon[JsonWriter[A]].write(conf).toString)

      val deployment = for {

        flakeDir <- command.local
                      .Command(
                        s"$name-flake-directory",
                        command.local.CommandArgs(
                          create = s"mkdir -p '$$XDG_STATE_HOME/biser/flakes/$name' && echo '$$XDG_STATE_HOME/biser/flakes/$name'",
                          delete = s"rm -rf '$$XDG_STATE_HOME/biser/flakes/$name'"
                        )
                      )
                      .stdout

        _ <- command.local
               .Command(
                 s"$name-deploy",
                 command.local.CommandArgs(
                   create =
                     p"""git clone --depth=1 ${params.flakeRepository} . && echo '$jsonConfig' > '${params.configLocation}' && nixos-rebuild switch < /dev/null --use-remote-sudo --target-host ${params.targetHost} --show-trace --flake ".#${params.flakeOutput}"""",
                   dir = flakeDir,
                   environment = Map(
                     "NIX_SSHOPTS" -> "-t -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
                   )
                 )
               )
               .stdout

      } yield flakeDir

      ConfigurableFlakeDeployment(deployment)
    }

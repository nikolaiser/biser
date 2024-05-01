package com.nikolaiser.biser.nix

import besom.*
import besom.api.command
import besom.internal.RegistersOutputs
import besom.json.JsonWriter
import scala.concurrent.Future
import Pulumi.given_ExecutionContext

case class ConfigurableFlakeDeployment(
    flakeDir: Output[String]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object ConfigurableFlakeDeployment:

  def apply[A: JsonWriter](using Context)(
      flakeRepository: String,
      flakeOutput: String,
      config: A,
      targetHost: String, // including usrname@
      name: String,
      configLocation: String = "./config.json",
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[ConfigurableFlakeDeployment] =
    component(
      s"""biser-flake-deployment-$name""",
      "biser:nix:ConfigurableFlakeDeployment",
      options
    ) {
      val jsonConfig = summon[JsonWriter[A]].write(config).toString

      val deployment = for {

        flakeDir <- command.local
          .Command(
            s"biser-flake-deployment-$name-flakedir",
            command.local.CommandArgs(
              create =
                s"mkdir -p '$$XDG_STATE_HOME/biser/flakes/$name' && echo '$$XDG_STATE_HOME/biser/flakes/$name'",
              delete = s"rm -rf '$$XDG_STATE_HOME/biser/flakes/$name'"
            )
          )
          .stdout

        _ <- command.local
          .Command(
            s"biser-flake-deployment-$flakeRepository-build",
            command.local.CommandArgs(
              create =
                s"""git clone --depth=1 $flakeRepository . && echo '$jsonConfig' > '$configLocation' && nixos-rebuild switch < /dev/null --use-remote-sudo --target-host $targetHost --show-trace --flake ".#$flakeOutput"""",
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

package com.nikolaiser.biser.nix

import besom.*
import besom.api.command.local.Command
import besom.api.command.local.CommandArgs
import besom.json.JsonWriter
import besom.internal.Result
import besom.json.JsString

def purgaDeployment[A](username: String, host: String, flake: String, config: A)(using writer: JsonWriter[A], ctx: Context) =
  val jsonConfig       = writer.write(config)
  val deployCmd        =
    s"""f=$$(mktemp); echo '${jsonConfig.toString}' > $$f ; nixos-rebuild switch < /dev/null --use-remote-sudo --target-host $username@$host --show-trace --flake "$flake" --override-input purgaArgs file+file://$$f --no-write-lock-file --refresh;rm -rf $$f"""
  val checkRevisionCmd = s"nix flake metadata ${flake.split("#").head} --no-write-lock-file --json --refresh | jq '.revision'"

  Command(
    s"$host-$flake-deploy",
    CommandArgs(
      create = deployCmd,
      update = deployCmd,
      environment = Map(
        "NIX_SSHOPTS" -> "-t -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
      ),
      triggers = List(
        jsonConfig,
        besom.internal.Output
          .apply(
            Result.blocking(
              os.proc("/bin/sh", "-c", checkRevisionCmd).spawn().stdout.trim()
            )
          )
          .map(JsString(_))
      )
    )
  )

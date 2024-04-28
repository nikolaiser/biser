package nikolaiser.biser.nix
import besom.*
import besom.api.command
import besom.internal.RegistersOutputs

case class FlakeBuild(
    path: Output[String]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object FlakeBuild:

  def apply(using Context)(
      flake: String,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[FlakeBuild] =
    component(
      s"""biser-nix-build-$flake""",
      "biser:nix:FlakeBuild",
      options
    ) {
      val flakeBuildCommand = command.local.Command(
        s"biser-nix-build-$flake",
        command.local.CommandArgs(
          create =
            s"""nix build $flake --no-link --json 2> /dev/null | jq '.[0].outputs.out' --raw-output"""
        )
      )

      FlakeBuild(flakeBuildCommand.stdout)
    }

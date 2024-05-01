package com.nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve
import besom.internal.RegistersOutputs

case class CloudInitVm private (
    vm: Output[proxmoxve.vm.VirtualMachine]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object CloudInitVm:

  case class Params(
      image: Input[proxmoxve.storage.File],
      config: Input[VmConfig]
  )

  def apply(using Context)(
      name: NonEmptyString,
      params: Params,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[CloudInitVm] =
    component(
      name,
      "biser:proxmox:CloudInitVm",
      options
    ) {
      val metaConfigFile = proxmoxve.storage.File(
        s"$name-meta-config",
        proxmoxve.storage.FileArgs(
          contentType = "snippets",
          datastoreId = "local",
          nodeName = params.config.asOutput().map(_.nodeName),
          sourceRaw = proxmoxve.storage.inputs.FileSourceRawArgs(
            data = p"""
            |dsmode: local
            |local-hostname: ${params.config.asOutput().map(_.hostname)}
            """.stripMargin,
            fileName = p"meta-config-${params.config.asOutput().map(_.hostname)}.yaml"
          )
        )
      )

      val vm = proxmoxve.vm.VirtualMachine(
        s"$name-vm",
        proxmoxve.vm.VirtualMachineArgs(
          name = params.config.asOutput().map(_.hostname),
          nodeName = params.config.asOutput().map(_.nodeName),
          tags = params.config.asOutput().map(_.tags),
          agent = proxmoxve.vm.inputs.VirtualMachineAgentArgs(
            enabled = params.config.asOutput().map(_.qemuAgentEnabled)
          ),
          cpu = proxmoxve.vm.inputs.VirtualMachineCpuArgs(
            cores = params.config.asOutput().map(_.cores),
            `type` = "host"
          ),
          memory = proxmoxve.vm.inputs.VirtualMachineMemoryArgs(
            dedicated = params.config.asOutput().map(_.memoryGb * 1024)
          ),
          initialization = proxmoxve.vm.inputs.VirtualMachineInitializationArgs(
            userAccount = proxmoxve.vm.inputs.VirtualMachineInitializationUserAccountArgs(
              username = params.config.asOutput().map(_.username),
              keys = params.config.asOutput().map(_.authorizedKeys)
            ),
            ipConfigs = List(
              proxmoxve.vm.inputs.VirtualMachineInitializationIpConfigArgs(
                ipv4 = proxmoxve.vm.inputs
                  .VirtualMachineInitializationIpConfigIpv4Args(
                    address = params.config.asOutput().map(_.ipv4Config.map(_.address).getOrElse("dhcp")),
                    gateway = params.config.asOutput().map(_.ipv4Config.map(_.gateway))
                  )
              )
            ),
            metaDataFileId = metaConfigFile.id
          ),
          disks = List(
            proxmoxve.vm.inputs.VirtualMachineDiskArgs(
              datastoreId = "local-lvm",
              fileId = params.image.asOutput().id,
              interface = "virtio0",
              size = params.config.asOutput().map(_.diskGb)
            )
          )
        )
      )

      CloudInitVm(vm)
    }

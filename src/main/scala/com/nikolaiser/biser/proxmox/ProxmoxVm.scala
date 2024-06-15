package com.nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve.Provider
import besom.api.proxmoxve.vm.VirtualMachine
import com.nikolaiser.biser.common.SshConfig
import besom.api.proxmoxve.vm.VirtualMachineArgs
import besom.api.proxmoxve.vm.inputs.VirtualMachineAgentArgs
import besom.api.proxmoxve.vm.inputs.VirtualMachineCpuArgs
import besom.api.proxmoxve.vm.inputs.VirtualMachineMemoryArgs
import besom.api.proxmoxve.vm.inputs.VirtualMachineDiskArgs
import izumi.distage.model.definition.With

trait ProxmoxVm:
  def vm: Output[VirtualMachine]

object ProxmoxVm:

  type Factory = VmInstanceParams => ProxmoxVm @With[Impl]

  case class Impl(
      vmInstanceParams: VmInstanceParams,
      sshConfig: SshConfig,
      vmConfig: VmConfig,
      nodeImageFactory: NodeImage.Factory,
      provider: Provider
  )(using Context)
      extends ProxmoxVm:
    def nodeBaseImageId = nodeImageFactory(vmInstanceParams.nodeName).file.id

    def vm: Output[VirtualMachine] = VirtualMachine(
      s"${vmInstanceParams.nodeName}-${vmInstanceParams.name}-vm",
      VirtualMachineArgs(
        name = s"${vmInstanceParams.nodeName}-${vmInstanceParams.name}",
        nodeName = vmInstanceParams.nodeName,
        tags = vmConfig.tags,
        agent = VirtualMachineAgentArgs(enabled = vmConfig.qemuAgentEnabled),
        cpu = VirtualMachineCpuArgs(cores = vmConfig.cores, `type` = "host"),
        memory = VirtualMachineMemoryArgs(dedicated = vmConfig.memoryGb * 1024),
        disks = List(
          VirtualMachineDiskArgs(
            datastoreId = "local-lvm",
            fileId = nodeBaseImageId,
            interface = "virtio0",
            size = vmConfig.diskGb
          )
        )
      ),
      opts(provider = provider, ignoreChanges = List("disks[0].speed", "cdrom"))
    )

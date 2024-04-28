package nikolaiser.biser.proxmox

import besom.*
import besom.api.proxmoxve
import besom.internal.RegistersOutputs

case class CloudInitVm(
    vm: Output[proxmoxve.vm.VirtualMachine]
)(using ComponentBase)
    extends ComponentResource
    derives RegistersOutputs

object CloudInitVm:

  def apply(using Context)(
      image: proxmoxve.storage.File,
      params: CoreVmParams,
      options: ComponentResourceOptions = ComponentResourceOptions()
  ): Output[CloudInitVm] =
    component(
      s"""biser-cloud-init-vm-${params.hostname}""",
      "biser:proxmox:CloudInitVm",
      options
    ) {
      val metaConfigFile = proxmoxve.storage.File(
        s"biser-meta-config-${params.hostname}",
        proxmoxve.storage.FileArgs(
          contentType = "snippets",
          datastoreId = "local",
          nodeName = params.nodeName,
          sourceRaw = proxmoxve.storage.inputs.FileSourceRawArgs(
            data = s"""
            |dsmode: local
            |local-hostname: ${params.hostname}
            """.stripMargin,
            fileName = s"meta-config-${params.hostname}.yaml"
          )
        )
      )

      val vm = proxmoxve.vm.VirtualMachine(
        s"biser-vm-${params.hostname}",
        proxmoxve.vm.VirtualMachineArgs(
          name = params.hostname,
          nodeName = params.nodeName,
          tags = params.tags,
          agent = proxmoxve.vm.inputs.VirtualMachineAgentArgs(
            enabled = params.qemuAgentEnabled
          ),
          cpu = proxmoxve.vm.inputs.VirtualMachineCpuArgs(
            cores = params.cores,
            `type` = "host"
          ),
          memory = proxmoxve.vm.inputs.VirtualMachineMemoryArgs(
            dedicated = params.memoryGb * 1024
          ),
          initialization = proxmoxve.vm.inputs.VirtualMachineInitializationArgs(
            userAccount =
              proxmoxve.vm.inputs.VirtualMachineInitializationUserAccountArgs(
                username = params.username,
                keys = params.authorizedKeys
              ),
            ipConfigs = List(
              proxmoxve.vm.inputs.VirtualMachineInitializationIpConfigArgs(
                ipv4 = proxmoxve.vm.inputs
                  .VirtualMachineInitializationIpConfigIpv4Args(
                    address =
                      params.ipv4Config.map(_.address).getOrElse("dhcp"),
                    gateway = params.ipv4Config.map(_.gateway)
                  )
              )
            ),
            metaDataFileId = metaConfigFile.id
          ),
          disks = List(
            proxmoxve.vm.inputs.VirtualMachineDiskArgs(
              datastoreId = "local-lvm",
              fileId = image.id,
              interface = "virtio0",
              size = params.diskGb
            )
          )
        )
      )

      CloudInitVm(vm)
    }

package com.nikolaiser.biser.proxmox

case class VmConfig(
    cores: Int,
    memoryGb: Int,
    diskGb: Int,
    username: String = "ops",
    tags: List[String] = Nil,
    qemuAgentEnabled: Boolean = true
)

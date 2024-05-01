package com.nikolaiser.biser.proxmox

case class VmConfig(
    hostname: String,
    nodeName: String,
    cores: Int,
    memoryGb: Int,
    diskGb: Int,
    username: String = "ops",
    authorizedKeys: List[String] = Nil,
    ipv4Config: Option[IpV4Config] = None,
    tags: List[String] = Nil,
    qemuAgentEnabled: Boolean = true
)

case class IpV4Config(address: String, gateway: String)

package com.xray.core.rust.client.xcra.dto

import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.IpUtil
import com.xray.core.rust.client.xcra.util.Utils


data class NodeItem(
    val configVersion: Int = 1,
    val configType: ConfigType,
    var groupId: String = "",
    var addedTime: Long = System.currentTimeMillis(),

    //general items
    var remarks: String = "",
    var address: String? = null,
    var port: String? = null,
    var username: String? = null,
    var password: String? = null,
    var uuid: String? = null,

    //vless items
    var vlessEncryption: String? = null,
    var vlessFlow: String? = null,

    //vmess items
    var vmessSecurity: String? = null,

    //shadow-socks items
    var shadowSocksMethod: String? = null,

    //tuic items
    var tuicCongestionControl: String? = null,
    var tuicUdpRelayMode: String? = null,
    var tuicHeartbeat: String? = null,

    //hysteria2 items
    var hysteria2ObfsPassword: String? = null,
    var hysteria2PortHopping: String? = null,
    var hysteria2PortHoppingInterval: String? = null,


    //transport items
    var transport: String? = null,
    var headerType: String? = null,
    var host: String? = null,
    var path: String? = null,
    var serviceName: String? = null,
    var xhttpMode: String? = null,

    //security items
    var security: String? = null,
    var sni: String? = null,
    var alpn: String? = null,
    var insecure: Boolean? = null,
    var publicKey: String? = null,
    var shortId: String? = null,

    ) {
    companion object {
        fun create(configType: ConfigType): NodeItem {
            return NodeItem(configType = configType)
        }
    }


    val description: String
        get() {
            val address = this.address?.let {
                if (it.contains(":"))
                    it.split(":")
                        .take(2)
                        .joinToString(":", postfix = ":***")
                else if (it.contains("."))
                    it.split('.')
                        .dropLast(1)
                        .joinToString(".", postfix = ".***")
                else
                    it
            }
            return "$address:${this.port}"
        }

    val subscriptionRemarks: String
        get() {
            val subRemarks =
                DatabaseHandler.decodeGroup(groupId)
                    ?.remarks
                    ?.firstOrNull()

            return subRemarks?.toString() ?: ""
        }
    val validPort: Int?
        get() {
            val port = Utils.parseInt(this.port)
            if (port in 1..<65535) {
                return port
            }
            return null
        }
    val addressConfig: String?
        get() {
            this.address?.let {
                if (IpUtil.isPureIpAddress(it)) {
                    return it
                }
                val resolve =
                    DatabaseHandler.decodeSettingsBool(AppConfig.PREF_RESOLVE_ADDRESS)
                if (!resolve) {
                    return it
                }
                val resolvedIps = HttpUtil.resolveHostToIP(
                    it,
                    DatabaseHandler.decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_IPV6)
                )
                if (!resolvedIps.isNullOrEmpty()) {
                    return resolvedIps.first()
                }
            }

            return this.address
        }
}
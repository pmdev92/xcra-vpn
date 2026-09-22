package com.xray.core.rust.client.xcra.dto

import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.IpUtil
import com.xray.core.rust.client.xcra.util.Utils

data class NodeItem(
    val configVersion: Int = 2,
    val configType: ConfigType,
    var groupId: String = "",
    var addedTime: Long = System.currentTimeMillis(),
    private var fields: MutableMap<String, String?>? = mutableMapOf(),
) {
    companion object {
        fun create(configType: ConfigType): NodeItem {
            return NodeItem(configType = configType)
        }
    }

    operator fun get(key: String): String? {
        return fields?.get(key)
    }

    operator fun set(key: String, value: String?) {
        if (fields == null) {
            fields = mutableMapOf();
        }
        if (value == null || value.isEmpty()) {
            fields?.remove(key)
        } else {
            fields?.set(key, value)
        }
    }

    // Computed
    val description: String
        get() {
            val address = this["address"]?.let {
                if (it.contains(":"))
                    it.split(":").take(2).joinToString(":", postfix = ":***")
                else if (it.contains("."))
                    it.split('.').dropLast(1).joinToString(".", postfix = ".***")
                else it
            }.orEmpty()
            if (!address.isEmpty()) {
                this["port"]?.let {
                    return "$address:${it}"
                }
            }
            return address
        }

    val subscriptionRemarks: String
        get() {
            val subRemarks = DatabaseHandler.decodeGroup(groupId)?.remarks?.firstOrNull()
            return subRemarks?.toString() ?: ""
        }

    val validPort: Int?
        get() {
            val port = Utils.parseInt(this["port"])
            return if (port in 1..<65535) port else null
        }

    val addressConfig: String?
        get() {
            this["address"]?.let {
                if (IpUtil.isPureIpAddress(it)) return it
                val resolve = DatabaseHandler.decodeSettingsBool(AppConfig.PREF_RESOLVE_ADDRESS)
                if (!resolve) return it
                val resolvedIps = HttpUtil.resolveHostToIP(
                    it,
                    DatabaseHandler.decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_IPV6)
                )
                if (!resolvedIps.isNullOrEmpty()) return resolvedIps.first()
            }
            return this["address"]
        }
}

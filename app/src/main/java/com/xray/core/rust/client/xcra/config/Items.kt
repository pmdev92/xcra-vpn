package com.xray.core.rust.client.xcra.config

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.getErrorMessage

open class Item(
    val key: String,
)

class DividerItem(
    key: String,
) : Item(key)

class TitleItem(
    key: String,
    var title: String
) : Item(key)

abstract class ConfigurableItem(
    key: String,
    val title: String,
    helperText: String? = null,
    errorMessage: String? = null,
) : Item(key) {
    var isError by mutableStateOf(false)
        private set
    var helperText by mutableStateOf(helperText)
        private set
    var errorMessage by mutableStateOf(errorMessage)
        private set

    internal fun setError() {
        if (!errorMessage.isNullOrEmpty()) {
            this.isError = true
        }
    }

    internal fun clearError() {
        this.isError = false
    }

    internal abstract fun updateValue(newValue: String)
    internal abstract fun getValue(): String
    internal abstract fun getDisplayValue(): String
}

class IntegerItem(
    key: String,
    title: String,
    initialValue: String = "",
    helperText: String?,
    errorMessage: String?,
) : ConfigurableItem(key, title, helperText, errorMessage) {

    private var value by mutableStateOf(initialValue)


    override fun updateValue(newValue: String) {
        if (newValue.isEmpty()) {
            value = newValue
            clearError()
        } else {
            val intValue = newValue.toIntOrNull()
            if (intValue != null) {
                value = "$intValue"
                clearError()
            } else {
                setError()
            }
        }
    }

    override fun getValue(): String {
        return value
    }

    override fun getDisplayValue(): String {
        return getValue()
    }

}

class TextItem(
    key: String,
    title: String,
    initialValue: String = "",
    helperText: String?,
    errorMessage: String?,
) : ConfigurableItem(key, title, helperText, errorMessage) {
    private var value by mutableStateOf(initialValue)


    override fun updateValue(newValue: String) {
        value = newValue
        clearError()
    }

    override fun getValue(): String {
        return value
    }

    override fun getDisplayValue(): String {
        return getValue()
    }
}

class BooleanDropDownItem(
    key: String,
    title: String,
    initialValue: String? = null,
    helperText: String?,
    errorMessage: String?,
) : ConfigurableItem(key, title, helperText, errorMessage) {
    val items: List<String> = listOf("", "false", "true")
    private var value by mutableStateOf(
        initialValue?.takeIf { it.isNotEmpty() }
            ?.let { if (it == "") "" else if (it == "0") "0" else if (it == "1") "1" else "0" }
            ?: items.firstOrNull()
            ?: ""
    )

    override fun updateValue(newValue: String) {
        value = newValue
        clearError()
    }

    override fun getValue(): String {
        return when (value.lowercase()) {
            "" -> ""
            "true" -> "1"
            "false" -> "0"
            "1" -> "1"
            "0" -> "0"
            else -> "0"
        }
    }

    override fun getDisplayValue(): String {
        return when (getValue()) {
            "" -> items[0]
            "1" -> items[2]
            "0" -> items[1]
            else -> items[1]
        }
    }
}

class JsonItem(
    key: String,
    title: String,
    initialValue: String = "",
    helperText: String? = null,
    errorMessage: String? = null,
) : ConfigurableItem(key, title, helperText, errorMessage) {
    private var value by mutableStateOf(initialValue)

    override fun updateValue(newValue: String) {
        value = format(newValue)
        clearError()
    }

    override fun getValue(): String {
        return value
    }

    override fun getDisplayValue(): String {
        return value
    }

    private fun format(s: String): String {
        val formatted = try {
            org.json.JSONObject(s).toString(4)
        } catch (_: Exception) {
            s
        }
        return formatted
    }
}

class DropDownItem(
    key: String,
    title: String,
    val items: List<String>,
    initialValue: String? = null,
    val isCapitalize: Boolean = false,
    helperText: String?,
    errorMessage: String?,
) : ConfigurableItem(key, title, helperText, errorMessage) {
    private var value by mutableStateOf(
        initialValue?.takeIf { it.isNotEmpty() }
            ?: items.firstOrNull()
            ?: ""
    )

    override fun updateValue(newValue: String) {
        value = newValue
        clearError()
    }

    override fun getValue(): String {
        return value
    }

    override fun getDisplayValue(): String {
        return getValue()
    }
}


private const val KEY_TITLE_GENERAL = "title_general"
private const val KEY_TITLE_CONFIGURATION = "title_configuration"
private const val KEY_TITLE_TRANSPORT = "title_transport"
private const val KEY_TITLE_SECURITY = "title_security"
private const val KEY_DIVIDER = "divider"

fun getProtocolKeys(node: NodeItem): List<String> {
    val keys = mutableListOf<String>()
    keys.add(KEY_TITLE_GENERAL)
    keys.add("remarks")
    keys.add("address")
    keys.add("port")
    keys.add(KEY_DIVIDER)
    keys.add(KEY_TITLE_CONFIGURATION)

    when (node.configType) {
        ConfigType.SOCKS5 -> {
            keys.add("username")
            keys.add("password")
        }

        ConfigType.VLESS -> {
            keys.add("uuid")
            keys.add("vless_encryption")
            keys.add("vless_flow")
        }

        ConfigType.VMESS -> {
            keys.add("uuid")
            keys.add("vmess_security")
        }

        ConfigType.TROJAN -> {
            keys.add("password")
        }

        ConfigType.SHADOWSOCKS -> {
            keys.add("password")
            keys.add("shadow_socks_method")
        }

        ConfigType.TUIC -> {
            keys.add("password")
            keys.add("uuid")
            keys.add("sni")
            keys.add("insecure")
            keys.add("alpn")
            keys.add("tuic_congestion_control")
            keys.add("tuic_udp_relay_mode")
            keys.add("tuic_heartbeat")
        }

        ConfigType.HYSTERIA2 -> {
            keys.add("password")
            keys.add("hysteria2_obfs_type")
            val obfsType = node["hysteria2_obfs_type"].orEmpty()
            if (obfsType.equals("salamander", true) || obfsType.equals("gecko", true)) {
                keys.add("hysteria2_obfs_password")
            }
            keys.add("sni")
            keys.add("insecure")
            keys.add("alpn")
            keys.add("hysteria2_port_hopping")
            keys.add("hysteria2_port_hopping_interval")
            if (obfsType.equals("gecko", true)) {
                keys.add("hysteria2_gecko_min_packet_len")
                keys.add("hysteria2_gecko_max_packet_len")
            }
        }
    }

    return keys
}

fun getTransportKeys(node: NodeItem): List<String> {
    val keys = mutableListOf<String>()
    keys.add(KEY_DIVIDER)
    keys.add(KEY_TITLE_TRANSPORT)
    keys.add("transport")

    val transportValue = node["transport"].orEmpty()
    val headerTypeValue = node["header_type"].orEmpty()

    when {
        transportValue.equals("tcp", true) -> {
            keys.add("header_type")
            if (headerTypeValue == "http") {
                keys.add("host")
                keys.add("path")
            }
        }

        transportValue.equals("websocket", true) -> {
            keys.add("host")
            keys.add("path")
        }

        transportValue.equals("httpupgrade", true) -> {
            keys.add("host")
            keys.add("path")
        }

        transportValue.equals("xhttp", true) -> {
            keys.add("x_http_mode")
            keys.add("host")
            keys.add("path")
            keys.add("extra_json")
        }

        transportValue.equals("http/2", true) -> {
            keys.add("host")
            keys.add("path")
        }

        transportValue.equals("grpc", true) -> {
            keys.add("service_name")
        }
    }

    return keys
}

fun getSecurityKeys(node: NodeItem): List<String> {
    val keys = mutableListOf<String>()
    keys.add("security")

    val securityValue = node["security"].orEmpty()
    when {
        securityValue.equals("tls", true) -> {
            keys.add("sni")
            keys.add("insecure")
            keys.add("pcs")
            keys.add("pcn")
            keys.add("alpn")
        }

        securityValue.equals("reality", true) -> {
            keys.add("sni")
            keys.add("public_key")
            keys.add("short_id")
            keys.add("alpn")
        }
    }
    return keys
}

fun createItemFromKey(application: Application, key: String): Item {
    return when (key) {
        KEY_TITLE_GENERAL -> TitleItem(key, application.getString(R.string.title_general))
        KEY_TITLE_CONFIGURATION -> TitleItem(
            key,
            application.getString(R.string.title_node_configuration)
        )

        KEY_TITLE_TRANSPORT -> TitleItem(key, application.getString(R.string.title_transport))
        KEY_TITLE_SECURITY -> TitleItem(key, application.getString(R.string.node_lab_security))
        KEY_DIVIDER -> DividerItem(key)

        "remarks" -> TextItem(
            key,
            application.getString(R.string.node_lab_remarks),
            helperText = "Leave empty to use auto generated remarks",
            errorMessage = application.getErrorMessage(R.string.node_lab_remarks)
        )

        "address" -> TextItem(
            key,
            application.getString(R.string.node_lab_address),
            helperText = "Server IP address or domain",
            errorMessage = application.getErrorMessage(R.string.node_lab_address)
        )

        "port" -> IntegerItem(
            key,
            application.getString(R.string.node_lab_port),
            helperText = "Port number (1-65535)",
            errorMessage = application.getErrorMessage(R.string.node_lab_port)
        )

        "username" -> TextItem(
            key,
            application.getString(R.string.node_lab_username),
            helperText = "Authentication username if required",
            errorMessage = application.getErrorMessage(R.string.node_lab_username)
        )

        "password" -> TextItem(
            key,
            application.getString(R.string.node_lab_password),
            helperText = "Password from your server provider",
            errorMessage = application.getErrorMessage(R.string.node_lab_password)
        )

        "uuid" -> TextItem(
            key,
            application.getString(R.string.node_lab_uuid),
            helperText = "UUID from your server provider",
            errorMessage = application.getErrorMessage(R.string.node_lab_uuid)
        )

        "vless_encryption" -> TextItem(
            key,
            application.getString(R.string.node_lab_vless_encryption),
            initialValue = "none",
            helperText = "Encryption method for VLESS",
            errorMessage = application.getErrorMessage(R.string.node_lab_vless_encryption)
        )

        "vless_flow" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_vless_flow),
            items = application.resources.getStringArray(R.array.flows).asList(),
            helperText = "xtls-rprx-vision recommended",
            errorMessage = application.getErrorMessage(R.string.node_lab_vless_flow)
        )

        "vmess_security" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_security),
            items = application.resources.getStringArray(R.array.vmess_securities).asList(),
            helperText = "Auto for most cases",
            errorMessage = application.getErrorMessage(R.string.node_lab_security)
        )

        "shadow_socks_method" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_security),
            items = application.resources.getStringArray(R.array.shadow_socks_methods).asList(),
            helperText = "Encryption method for ShadowSocks",
            errorMessage = application.getErrorMessage(R.string.node_lab_security)
        )

        "tuic_congestion_control" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_tuic_congestion_control),
            items = application.resources.getStringArray(R.array.tuic_congestion_control).asList(),
            helperText = "Congestion control algorithm",
            errorMessage = application.getErrorMessage(R.string.node_lab_tuic_congestion_control)
        )

        "tuic_udp_relay_mode" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_tuic_udp_relay_mode),
            items = application.resources.getStringArray(R.array.tuic_udp_relay_mode).asList(),
            helperText = "UDP relay mode",
            errorMessage = application.getErrorMessage(R.string.node_lab_tuic_udp_relay_mode)
        )

        "tuic_heartbeat" -> TextItem(
            key,
            application.getString(R.string.node_lab_tuic_heartbeat),
            initialValue = "10s",
            helperText = "Keep-alive interval (e.g. 10s)",
            errorMessage = application.getErrorMessage(R.string.node_lab_tuic_heartbeat)
        )

        "hysteria2_obfs_type" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_obfs_type),
            items = application.resources.getStringArray(R.array.hysteria2_obfs_types).asList(),
            helperText = "Gecko obfuscation type (none/salamander/gecko)",
            errorMessage = application.getErrorMessage(R.string.node_lab_obfs_type)
        )

        "hysteria2_obfs_password" -> TextItem(
            key,
            application.getString(R.string.node_lab_obfs_password),
            helperText = "Gecko obfuscation password (required for salamander/gecko)",
            errorMessage = application.getErrorMessage(R.string.node_lab_obfs_password)
        )

        "hysteria2_port_hopping" -> TextItem(
            key,
            application.getString(R.string.node_lab_port_hopping),
            helperText = "Port hopping enabled",
            errorMessage = application.getErrorMessage(R.string.node_lab_port_hopping)
        )

        "hysteria2_port_hopping_interval" -> TextItem(
            key,
            application.getString(R.string.node_lab_port_hopping_interval),
            helperText = "Port hopping interval",
            errorMessage = application.getErrorMessage(R.string.node_lab_port_hopping_interval)
        )

        "hysteria2_gecko_min_packet_len" -> IntegerItem(
            key,
            application.getString(R.string.node_lab_min_packet_len),
            initialValue = "512",
            helperText = "Gecko min packet length (default 512)",
            errorMessage = application.getErrorMessage(R.string.node_lab_min_packet_len)
        )

        "hysteria2_gecko_max_packet_len" -> IntegerItem(
            key,
            application.getString(R.string.node_lab_max_packet_len),
            initialValue = "1200",
            helperText = "Gecko max packet length (default 1200)",
            errorMessage = application.getErrorMessage(R.string.node_lab_max_packet_len)
        )

        "transport" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_transport),
            items = application.resources.getStringArray(R.array.transports).asList(),
            helperText = "Transport protocol",
            errorMessage = application.getErrorMessage(R.string.node_lab_transport)
        )

        "header_type" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_tcp_header_type),
            items = application.resources.getStringArray(R.array.tcp_header_type).asList(),
            helperText = "TCP header type (none/http)",
            errorMessage = application.getErrorMessage(R.string.node_lab_tcp_header_type)
        )

        "host" -> TextItem(
            key,
            application.getString(R.string.node_lab_host),
            helperText = "Host header value",
            errorMessage = application.getErrorMessage(R.string.node_lab_host)
        )

        "path" -> TextItem(
            key,
            application.getString(R.string.node_lab_path),
            helperText = "Path value",
            errorMessage = application.getErrorMessage(R.string.node_lab_path)
        )

        "x_http_mode" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_xhttp_mode),
            items = application.resources.getStringArray(R.array.xhttp_mode).asList(),
            isCapitalize = true,
            helperText = "XHTTP mode",
            errorMessage = application.getErrorMessage(R.string.node_lab_xhttp_mode)
        )

        "service_name" -> TextItem(
            key,
            application.getString(R.string.node_lab_service_name),
            helperText = "Service name for gRPC",
            errorMessage = application.getErrorMessage(R.string.node_lab_service_name)
        )

        "extra_json" -> JsonItem(
            "extra_json",
            application.getString(R.string.node_lab_extra_json),
            helperText = "JSON config for XHTTP (editable)",
            errorMessage = application.getErrorMessage(R.string.node_lab_extra_json)
        )

        "security" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_security),
            items = application.resources.getStringArray(R.array.securities).asList(),
            helperText = "Security protocol",
            errorMessage = application.getErrorMessage(R.string.node_lab_security)
        )

        "sni" -> TextItem(
            key,
            application.getString(R.string.node_lab_sni),
            helperText = "Server Name Indication",
            errorMessage = application.getErrorMessage(R.string.node_lab_sni)
        )

        "insecure" -> BooleanDropDownItem(
            key,
            application.getString(R.string.node_lab_insecure),
            helperText = "Allow insecure TLS connections",
            errorMessage = application.getErrorMessage(R.string.node_lab_insecure)
        )

        "pcs" -> TextItem(
            key,
            application.getString(R.string.node_lab_pcs),
            helperText = "Pinned peer cert SHA256 (comma separated). Not applied in insecure mode.",
            errorMessage = application.getErrorMessage(R.string.node_lab_pcs)
        )

        "pcn" -> TextItem(
            key,
            application.getString(R.string.node_lab_pcn),
            helperText = "Verify peer cert by name (comma separated). Not applied in insecure mode.",
            errorMessage = application.getErrorMessage(R.string.node_lab_pcn)
        )

        "alpn" -> DropDownItem(
            key,
            application.getString(R.string.node_lab_security_alpn),
            items = application.resources.getStringArray(R.array.security_alpn).asList(),
            helperText = "ALPN protocol (h2, http/1.1)",
            errorMessage = application.getErrorMessage(R.string.node_lab_security_alpn)
        )

        "public_key" -> TextItem(
            key,
            application.getString(R.string.node_lab_public_key),
            helperText = "Reality public key",
            errorMessage = application.getErrorMessage(R.string.node_lab_public_key)
        )

        "short_id" -> TextItem(
            key,
            application.getString(R.string.node_lab_short_id),
            helperText = "Reality short ID",
            errorMessage = application.getErrorMessage(R.string.node_lab_short_id)
        )


        else -> DividerItem(key)
    }
}

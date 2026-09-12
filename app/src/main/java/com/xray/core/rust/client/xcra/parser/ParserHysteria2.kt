package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserHysteria2 : Parser() {


    /**
     * Parses a Hysteria2 URI string into a NodeItem object.
     *
     * @param str the Hysteria2 URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.HYSTERIA2)

        val uri = URI(Utils.fixIllegalUrl(str))
        config["remarks"] =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config["address"] = uri.idnHost
        config["port"] = uri.port.toString()
        config["password"] = uri.userInfo
        config["security"] = AppConfig.TLS

        if (!uri.rawQuery.isNullOrEmpty()) {
            val queryParam = getQueryParam(uri)

            getTransportFormQuery(config, queryParam)

            config["security"] = queryParam["security"] ?: AppConfig.TLS
            config["hysteria2_obfs_type"] = queryParam["obfs"] ?: "none"
            config["hysteria2_obfs_password"] = queryParam["obfs-password"]

            val obfsType = config["hysteria2_obfs_type"].orEmpty()
            if (obfsType.equals("gecko", true)) {
                config["hysteria2_gecko_min_packet_len"] = queryParam["minPacketSize"]
                config["hysteria2_gecko_max_packet_len"] = queryParam["maxPacketSize"]
            }
            config["hysteria2_port_hopping"] = queryParam["mport"]
            if (config["hysteria2_port_hopping"].isNotNullEmpty()) {
                config["hysteria2_port_hopping_interval"] = queryParam["mportHopInt"]
            }
        }

        return config
    }

    /**
     * Converts a NodeItem object to a URI string.
     *
     * @param config the NodeItem object to convert
     * @return the converted URI string
     */
    fun toUri(config: NodeItem): String {
        val dicQuery = HashMap<String, String>()
        config["security"].let { if (it != null) dicQuery["security"] = it }
        config["sni"].let { if (it.isNotNullEmpty()) dicQuery["sni"] = it.orEmpty() }
        config["alpn"].let { if (it.isNotNullEmpty()) dicQuery["alpn"] = it.orEmpty() }
        config["insecure"].let { dicQuery["insecure"] = if (it == "1") "1" else "0" }

        val obfsType = config["hysteria2_obfs_type"].orEmpty()
        if (obfsType.isNotEmpty() && obfsType != "none") {
            dicQuery["obfs"] = obfsType
            config["hysteria2_obfs_password"]?.let {
                if (it.isNotEmpty()) {
                    dicQuery["obfs-password"] = it
                }
            }
            if (obfsType.equals("gecko", true)) {
                config["hysteria2_gecko_min_packet_len"]?.let {
                    if (it.isNotEmpty()) {
                        dicQuery["minPacketSize"] = it
                    }
                }
                config["hysteria2_gecko_max_packet_len"]?.let {
                    if (it.isNotEmpty()) {
                        dicQuery["maxPacketSize"] = it
                    }
                }
            }
        }

        if (config["hysteria2_port_hopping"].isNotNullEmpty()) {
            dicQuery["mport"] = config["hysteria2_port_hopping"].orEmpty()
        }
        if (config["hysteria2_port_hopping_interval"].isNotNullEmpty()) {
            dicQuery["mportHopInt"] = config["hysteria2_port_hopping_interval"].orEmpty()
        }

        return toUri(config, config["password"], dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        val obfsType = nodeItem["hysteria2_obfs_type"].orEmpty()
        val obfsPassword = nodeItem["hysteria2_obfs_password"].orEmpty()

        val actualObfsType = if (obfsType.isEmpty() || obfsType == "none") "" else obfsType

        val allowInsecure = decideAllowInsecure(nodeItem)

        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.HYSTERIA2)
            outbound.settings = Outbound.Hysteria2Settings(
                address = nodeItem.addressConfig,
                port = it,
                password = nodeItem["password"],
                obfsType = actualObfsType,
                obfsPassword = obfsPassword,
                geckoMinPacketLen = if (actualObfsType == "gecko") nodeItem["hysteria2_gecko_min_packet_len"]?.toIntOrNull() else null,
                geckoMaxPacketLen = if (actualObfsType == "gecko") nodeItem["hysteria2_gecko_max_packet_len"]?.toIntOrNull() else null,
                tlsSettings = Outbound.TlsSettings(
                    serverName = nodeItem["sni"],
                    verify = !allowInsecure
                ),
            )
            return outbound
        }
        return null
    }
}
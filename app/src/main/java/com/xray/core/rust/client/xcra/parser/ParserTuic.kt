package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserTuic : Parser() {
    /**
     * Parses a Tuic URI string into a NodeItem object.
     *
     * @param str the Tuic URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.TUIC)

        val uri = URI(Utils.fixIllegalUrl(str))
        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()
        val parts = uri.userInfo.split(":", limit = 2)
        val uuid = parts.getOrNull(0)
        val password = parts.getOrNull(1)
        if (uuid == null || password == null) {
            return null
        }
        config.password = password
        config.uuid = uuid
        config.security = AppConfig.TLS
        if (!uri.rawQuery.isNullOrEmpty()) {
            val queryParam = getQueryParam(uri)
            getTransportFormQuery(config, queryParam)

            config.security = queryParam["security"] ?: AppConfig.TLS
            config.tuicCongestionControl = queryParam["congestion_control"]
            config.tuicHeartbeat = queryParam["heartbeat"]
            config.tuicUdpRelayMode = queryParam["udp_relay_mode"]
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
        config.security.let { if (it != null) dicQuery["security"] = it }
        config.sni.let { if (it.isNotNullEmpty()) dicQuery["sni"] = it.orEmpty() }
        config.alpn.let { if (it.isNotNullEmpty()) dicQuery["alpn"] = it.orEmpty() }
        config.tuicCongestionControl.let {
            if (it.isNotNullEmpty()) dicQuery["congestion_control"] = it.orEmpty()
        }
        config.tuicHeartbeat.let {
            if (it.isNotNullEmpty()) dicQuery["heartbeat"] = it.orEmpty()
        }
        config.tuicUdpRelayMode.let {
            if (it.isNotNullEmpty()) dicQuery["udp_relay_mode"] = it.orEmpty()
        }
        config.insecure.let { dicQuery["insecure"] = if (it == true) "1" else "0" }


        return toUri(config, config.password, dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        val allowInsecure = decideAllowInsecure(nodeItem)
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.TUIC)
            outbound.settings = Outbound.TuicSettings(
                address = nodeItem.addressConfig,
                port = it,
                password = nodeItem.password,
                uuid = nodeItem.uuid,
                heartbeat = nodeItem.tuicHeartbeat,
                congestionControl = nodeItem.tuicCongestionControl,
                udpRelayMode = nodeItem.tuicUdpRelayMode,
                tlsSettings = Outbound.TlsSettings(
                    serverName = nodeItem.sni,
                    verify = !allowInsecure
                ),
            )
            return outbound
        }
        return null
    }
}
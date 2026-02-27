package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.enums.TransportType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserTrojan : Parser() {

    /**
     * Parses a Trojan URI string into a NodeItem object.
     *
     * @param str the Trojan URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.TROJAN)

        val uri = URI(Utils.fixIllegalUrl(str))
        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()
        config.password = uri.userInfo


        if (uri.rawQuery.isNullOrEmpty()) {
            config.transport = TransportType.TCP.type
            config.security = AppConfig.TLS
        } else {
            val queryParam = getQueryParam(uri)
            getTransportFormQuery(config, queryParam)
            config.security = queryParam["security"] ?: AppConfig.TLS
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
        val dicQuery = getQueryTransportDic(config)
        return toUri(config, config.password, dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.TROJAN)
            outbound.settings = Outbound.TrojanSetting(
                address = nodeItem.addressConfig,
                port = it,
                password = nodeItem.password
            )
            populateTransportSettings(outbound, nodeItem)
            populateSecuritySettings(outbound, nodeItem)

            return outbound
        }
        return null
    }
}
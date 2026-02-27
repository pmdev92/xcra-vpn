package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserSocks5 : Parser() {

    /**
     * Parses a Socks5 URI string into a NodeItem object.
     *
     * @param str the Socks5 URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {

        val config = NodeItem.create(ConfigType.SOCKS5)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()

        val queryParam = getQueryParam(uri)
        getTransportFormQuery(config, queryParam)
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

        return toUri(config, ":", dicQuery)
    }

    /**
     * Converts a nodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.SOCKS5)
            outbound.settings = Outbound.Socks5Setting(
                address = nodeItem.addressConfig,
                port = it,
                username = nodeItem.username,
                password = nodeItem.password,
            )
            populateTransportSettings(outbound, nodeItem)
            populateSecuritySettings(outbound, nodeItem)
            return outbound
        }
        return null
    }
}
package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.util.Base64Util
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserShadowSocks : Parser() {


    /**
     * Parses a ShadowSocks URI string into a NodeItem object.
     *
     * @param str the ShadowSocks URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.SHADOWSOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null
        if (uri.userInfo.isNullOrEmpty()) return null



        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { if (it.isEmpty()) "none" else it }
        config.address = uri.idnHost
        config.port = uri.port.toString()

        val result = if (uri.userInfo.contains(":")) {
            uri.userInfo.split(":", limit = 2)
        } else {
            Base64Util.decode(uri.userInfo).split(":", limit = 2)
        }
        if (result.count() == 2) {
            config.password = result.last()
            config.shadowSocksMethod = result.first()
        }


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
        val pw = "${config.shadowSocksMethod}:${config.password}"

        return toUri(config, Base64Util.encode(pw, true), dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.SHADOWSOCKS)
            outbound.settings = Outbound.ShadowSocksSetting(
                address = nodeItem.addressConfig,
                port = it,
                password = nodeItem.password,
                method = nodeItem.shadowSocksMethod,
            )
            populateTransportSettings(outbound, nodeItem)
            populateSecuritySettings(outbound, nodeItem)
            return outbound
        }
        return null
    }
}
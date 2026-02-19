package com.xray.core.rust.client.xcra.parser


import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserVless : Parser() {

    /**
     * Parses a Vless URI string into a NodeItem object.
     *
     * @param str the Vless URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.VLESS)
        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)
        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()
        config.uuid = uri.userInfo
        config.vlessEncryption = queryParam["encryption"] ?: "none"
        config.vlessFlow = queryParam["flow"] ?: ""
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
        dicQuery["encryption"] = config.vlessEncryption ?: "none"
        dicQuery["flow"] = config.vlessFlow ?: ""

        return toUri(config, config.uuid, dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.VLESS)
            outbound.settings = Outbound.VlessSetting(
                address = nodeItem.addressConfig,
                port = it,
                id = nodeItem.uuid,
                encryption = nodeItem.vlessEncryption,
                flow = nodeItem.vlessFlow,
            )
            populateTransportSettings(outbound, nodeItem)
            populateSecuritySettings(outbound, nodeItem)


            return outbound
        }
        return null
    }
}
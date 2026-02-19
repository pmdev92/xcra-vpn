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
        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()
        config.password = uri.userInfo
        config.security = AppConfig.TLS

        if (!uri.rawQuery.isNullOrEmpty()) {
            val queryParam = getQueryParam(uri)

            getTransportFormQuery(config, queryParam)

            config.security = queryParam["security"] ?: AppConfig.TLS
            config.hysteria2ObfsPassword = queryParam["obfs-password"]
            config.hysteria2PortHopping = queryParam["mport"]
            if (config.hysteria2PortHopping.isNotNullEmpty()) {
                config.hysteria2PortHoppingInterval = queryParam["mportHopInt"]
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
        config.security.let { if (it != null) dicQuery["security"] = it }
        config.sni.let { if (it.isNotNullEmpty()) dicQuery["sni"] = it.orEmpty() }
        config.alpn.let { if (it.isNotNullEmpty()) dicQuery["alpn"] = it.orEmpty() }
        config.insecure.let { dicQuery["insecure"] = if (it == true) "1" else "0" }

        if (config.hysteria2ObfsPassword.isNotNullEmpty()) {
            dicQuery["obfs"] = "salamander"
            dicQuery["obfs-password"] = config.hysteria2ObfsPassword.orEmpty()
        }
        if (config.hysteria2PortHopping.isNotNullEmpty()) {
            dicQuery["mport"] = config.hysteria2PortHopping.orEmpty()
        }
        if (config.hysteria2PortHoppingInterval.isNotNullEmpty()) {
            dicQuery["mportHopInt"] = config.hysteria2PortHoppingInterval.orEmpty()
        }

        return toUri(config, config.password, dicQuery)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        var obfsType = ""
        val obfsPassword = nodeItem.hysteria2ObfsPassword.orEmpty()
        if (obfsPassword.isNotEmpty()) {
            obfsType = "salamander"
        }
        val allowInsecure = decideAllowInsecure(nodeItem)

        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.HYSTERIA2)
            outbound.settings = Outbound.Hysteria2Settings(
                address = nodeItem.addressConfig,
                port = it,
                password = nodeItem.password,
                obfsType = obfsType,
                obfsPassword = obfsPassword,
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
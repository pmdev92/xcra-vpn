package com.xray.core.rust.client.xcra.parser

import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.AllowInsecure
import com.xray.core.rust.client.xcra.enums.TransportType
import com.xray.core.rust.client.xcra.enums.TransportType.GRPC
import com.xray.core.rust.client.xcra.enums.TransportType.H2
import com.xray.core.rust.client.xcra.enums.TransportType.HTTP_UPGRADE
import com.xray.core.rust.client.xcra.enums.TransportType.TCP
import com.xray.core.rust.client.xcra.enums.TransportType.WS
import com.xray.core.rust.client.xcra.enums.TransportType.XHTTP
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

open class Parser {
    /**
     * Converts a NodeItem object to a URI string.
     *
     * @param config the NodeItem object to convert
     * @param userInfo the user information to include in the URI
     * @param dicQuery the query parameters to include in the URI
     * @return the converted URI string
     */
    fun toUri(config: NodeItem, userInfo: String?, dicQuery: HashMap<String, String>?): String {
        val query = if (dicQuery != null)
            "?" + dicQuery.toList().joinToString(
                separator = "&",
                transform = { it.first + "=" + Utils.urlEncode(it.second) })
        else ""

        val url = String.format(
            "%s@%s:%s",
            Utils.urlEncode(userInfo ?: ""),
            Utils.getIpv6Address(HttpUtil.toIdnDomain(config.address.orEmpty())),
            config.port
        )

        return "${url}${query}#${Utils.urlEncode(config.remarks)}"
    }

    /**
     * Extracts query parameters from a URI.
     *
     * @param uri the URI to extract query parameters from
     * @return a map of query parameters
     */
    fun getQueryParam(uri: URI): Map<String, String> {
        return uri.rawQuery.split("&")
            .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }
    }

    /**
     * Populates a NodeItem object with values from query parameters.
     *
     * @param config the NodeItem object to populate
     * @param queryParam the query parameters to use for populating the NodeItem
     * @param allowInsecure whether to allow insecure connections
     */
    fun getTransportFormQuery(
        config: NodeItem,
        queryParam: Map<String, String>,
    ) {
        config.transport = queryParam["type"] ?: TransportType.TCP.type
        config.headerType = queryParam["headerType"]
        config.host = queryParam["host"]
        config.path = queryParam["path"]

        config.serviceName = queryParam["serviceName"]
        config.xhttpMode = queryParam["mode"]

        config.security = queryParam["security"]
        if (config.security != AppConfig.TLS && config.security != AppConfig.REALITY) {
            config.security = null
        }
        // Support multiple possible query keys for allowInsecure like the C# implementation
        val allowInsecureKeys = arrayOf("insecure", "allowInsecure", "allow_insecure")
        config.insecure = when {
            allowInsecureKeys.any { queryParam[it] == "1" } -> true
            allowInsecureKeys.any { queryParam[it] == "0" } -> false
            else -> {
                null
            }
        }
        config.sni = queryParam["sni"]
        config.alpn = queryParam["alpn"]
        config.publicKey = queryParam["pbk"]
        config.shortId = queryParam["sid"]
    }

    /**
     * Creates a map of query parameters from a NodeItem object.
     *
     * @param config the NodeItem object to create query parameters from
     * @return a map of query parameters
     */
    fun getQueryTransportDic(config: NodeItem): HashMap<String, String> {
        val dicQuery = HashMap<String, String>()
        dicQuery["security"] = config.security?.ifEmpty { "none" }.orEmpty()
        config.sni.let { if (it.isNotNullEmpty()) dicQuery["sni"] = it.orEmpty() }
        config.alpn.let { if (it.isNotNullEmpty()) dicQuery["alpn"] = it.orEmpty() }
        config.publicKey.let { if (it.isNotNullEmpty()) dicQuery["pbk"] = it.orEmpty() }
        config.shortId.let { if (it.isNotNullEmpty()) dicQuery["sid"] = it.orEmpty() }
        // Add two keys for compatibility: "insecure" and "allowInsecure"
        if (config.security == AppConfig.TLS) {
            val insecureFlag = if (config.insecure == true) "1" else "0"
            dicQuery["insecure"] = insecureFlag
            dicQuery["allowInsecure"] = insecureFlag
        }

        val networkType = TransportType.fromString(config.transport)
        dicQuery["type"] = networkType.type
        when (networkType) {
            TCP -> {
                dicQuery["headerType"] = config.headerType?.ifEmpty { "none" }.orEmpty()
                config.host.let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
            }


            WS, HTTP_UPGRADE -> {
                config.host.let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config.path.let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
            }

            XHTTP -> {
                config.host.let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config.path.let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
                config.xhttpMode.let { if (it.isNotNullEmpty()) dicQuery["mode"] = it.orEmpty() }
            }

            H2 -> {
                dicQuery["type"] = "http"
                config.host.let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config.path.let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
            }

            GRPC -> {
                config.serviceName.let {
                    if (it.isNotNullEmpty()) dicQuery["serviceName"] = it.orEmpty()
                }
            }
        }
        return dicQuery
    }


    /**
     * Configures transport settings for an outbound connection.
     *
     * Sets up protocol-specific transport options based on the node settings.
     *
     * @param outbound The outbound to configure
     * @param nodeItem The node containing transport configuration
     * @return The Server Name Indication (SNI) value to use, or null if not applicable
     */
    fun populateTransportSettings(outbound: Outbound, nodeItem: NodeItem) {
        val transport = nodeItem.transport.orEmpty()
        val headerType = nodeItem.headerType
        val host = nodeItem.host
        val path = nodeItem.path
        val serviceName = nodeItem.serviceName
        val xhttpMode = nodeItem.xhttpMode

        val streamSettings = if (outbound.streamSettings != null) {
            outbound.streamSettings!!
        } else {
            val streamSettings = Outbound.StreamSetting()
            outbound.streamSettings = streamSettings
            streamSettings
        }

        streamSettings.transport = transport.ifEmpty { TCP.type }
        when (streamSettings.transport) {
            TCP.type -> {
                val tcpSettings = Outbound.TcpSettings()
                if (headerType == "http") {
                    tcpSettings.type = "http"
                    val request = Outbound.TcpSettings.TcpRequest()
                    request.path = path ?: "/"

                    if (host.isNotNullEmpty()) {
                        request.headers["Host"] = host as String
                    }
                    tcpSettings.request = request
                } else {
                    tcpSettings.type = "none"
                }
                streamSettings.tcpSettings = tcpSettings
            }

            WS.type -> {
                val wsSettings = Outbound.WsSettings()
                wsSettings.host = host.orEmpty()
                wsSettings.path = path ?: "/"
                streamSettings.wsSettings = wsSettings
            }

            HTTP_UPGRADE.type -> {
                val httpUpgradeSettings = Outbound.HttpUpgradeSettings()
                httpUpgradeSettings.host = host.orEmpty()
                httpUpgradeSettings.path = path ?: "/"
                streamSettings.httpUpgradeSettings = httpUpgradeSettings
            }

            XHTTP.type -> {
                val xHttpSettings = Outbound.XHttpSettings()
                xHttpSettings.host = host.orEmpty()
                xHttpSettings.path = path ?: "/"
                xHttpSettings.mode = xhttpMode
                streamSettings.xHttpSettings = xHttpSettings
            }

            H2.type -> {
                val http2Settings = Outbound.Http2Settings()
                http2Settings.host = host.orEmpty()
                http2Settings.path = path ?: "/"
                streamSettings.http2Settings = http2Settings
            }


            GRPC.type -> {
                val grpcSettings = Outbound.GrpcSettings()
                grpcSettings.serviceName = serviceName
                streamSettings.grpcSettings = grpcSettings
            }
        }
        outbound.streamSettings = streamSettings
    }

    /**
     * Configures TLS or REALITY security settings for an outbound connection.
     *
     * Sets up security-related parameters like certificates, fingerprints, and SNI.
     *
     * @param outbound The outbound to configure
     * @param nodeItem The node containing security configuration
     */
    fun populateSecuritySettings(
        outbound: Outbound,
        nodeItem: NodeItem,
    ) {
        val security = nodeItem.security.orEmpty()
        val allowInsecure = decideAllowInsecure(nodeItem)
        val sni = nodeItem.sni

        val alpns = nodeItem.alpn
        val alpnsArray = if (alpns.isNullOrEmpty()) null else alpns.split(",").map { it.trim() }
            .filter { it.isNotEmpty() }
        val publicKey = nodeItem.publicKey
        val shortId = nodeItem.shortId

        val streamSettings = if (outbound.streamSettings != null) {
            outbound.streamSettings!!
        } else {
            val streamSettings = Outbound.StreamSetting()
            outbound.streamSettings = streamSettings
            streamSettings
        }

        streamSettings.security = security.ifEmpty { "none" }
        if (streamSettings.security == "none") return

        if (streamSettings.security == AppConfig.TLS) {
            streamSettings.realitySettings = null
            streamSettings.tlsSettings = Outbound.TlsSettings()
            streamSettings.tlsSettings?.serverName = sni
            streamSettings.tlsSettings?.verify = !allowInsecure
            streamSettings.tlsSettings?.alpn = alpnsArray
        }
        if (streamSettings.security == AppConfig.REALITY) {
            streamSettings.tlsSettings = null
            streamSettings.realitySettings = Outbound.RealitySettings()
            streamSettings.realitySettings?.serverName = sni
            streamSettings.realitySettings?.verify = false
            streamSettings.realitySettings?.publicKey = publicKey
            streamSettings.realitySettings?.shortId = shortId
            streamSettings.tlsSettings?.alpn = alpnsArray
        }
    }

    fun decideAllowInsecure(nodeItem: NodeItem): Boolean {
        val allowInsecureStr = DatabaseHandler.decodeSettingsString(AppConfig.PREF_ALLOW_INSECURE)
            ?: AppConfig.DEFAULT_ALLOW_INSECURE

        val allowInsecure = AllowInsecure.fromString(allowInsecureStr)
        return when (allowInsecure) {
            AllowInsecure.FOLLOW_CONFIGURATION -> {
                nodeItem.insecure == true
            }

            AllowInsecure.FORCE_INSECURE -> {
                true
            }

            AllowInsecure.FORCE_SECURE -> {

                false
            }
        }
    }
}

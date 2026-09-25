package com.xray.core.rust.client.xcra.parser

import com.xray.core.rust.client.xcra.App
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
import com.xray.core.rust.client.xcra.extension.optBooleanOrNull
import com.xray.core.rust.client.xcra.extension.optJSONObjectOrNull
import com.xray.core.rust.client.xcra.extension.optLongOrNull
import com.xray.core.rust.client.xcra.extension.optStringOrNull
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

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
            Utils.getIpv6Address(HttpUtil.toIdnDomain(config["address"].orEmpty())),
            config["port"]
        )

        return "${url}${query}#${Utils.urlEncode(config["remarks"].orEmpty())}"
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
     */
    private fun parseXHttpExtraFromNode(jsonStr: String?): Outbound.XHttpConfig.XHttpConfigExtra? {

        val jsonStr = jsonStr ?: return null
        return try {
            val extraJson = org.json.JSONObject(jsonStr)
            val extra = Outbound.XHttpConfig.XHttpConfigExtra()

            val headersJson = extraJson.optJSONObjectOrNull("headers")
            if (headersJson != null) {
                val map = HashMap<String, String>()
                headersJson.keys().forEach { k -> map[k] = headersJson.optString(k) }
                extra.headers = map;
            }

            extra.noGrpcHeader = extraJson.optBooleanOrNull("noGrpcHeader")
            extra.xPaddingBytes = extraJson.optStringOrNull("xPaddingBytes")
            extra.xPaddingObfsMode = extraJson.optBooleanOrNull("xPaddingObfsMode")
            extra.xPaddingKey = extraJson.optStringOrNull("xPaddingKey")
            extra.xPaddingHeader = extraJson.optStringOrNull("xPaddingHeader")
            extra.xPaddingPlacement = extraJson.optStringOrNull("xPaddingPlacement")
            extra.xPaddingMethod = extraJson.optStringOrNull("xPaddingMethod")
            extra.scMaxEachPostBytes = extraJson.optStringOrNull("scMaxEachPostBytes")
            extra.scMinPostsIntervalMs = extraJson.optStringOrNull("scMinPostsIntervalMs")
            extra.uplinkHttpMethod = extraJson.optStringOrNull("uplinkHttpMethod")
            extra.uplinkDataPlacement = extraJson.optStringOrNull("uplinkDataPlacement")
            extra.uplinkDataKey = extraJson.optStringOrNull("uplinkDataKey")
            extra.uplinkChunkSize = extraJson.optStringOrNull("uplinkChunkSize")
            extra.sessionPlacement = extraJson.optStringOrNull("sessionPlacement")
            extra.sessionKey = extraJson.optStringOrNull("sessionKey")
            extra.seqPlacement = extraJson.optStringOrNull("seqPlacement")
            extra.seqKey = extraJson.optStringOrNull("seqKey")
            val xmuxJson = extraJson.optJSONObjectOrNull("xmux")
            if (xmuxJson != null) {
                val xmux = Outbound.XHttpConfig.XHttpConfigXmux()
                xmux.maxConcurrency = xmuxJson.optStringOrNull("maxConcurrency")
                xmux.maxConnections = xmuxJson.optStringOrNull("maxConnections")
                xmux.cMaxReuseTimes = xmuxJson.optStringOrNull("cMaxReuseTimes")
                xmux.hMaxRequestTimes = xmuxJson.optStringOrNull("hMaxRequestTimes")
                xmux.hMaxReusableSecs = xmuxJson.optStringOrNull("hMaxReusableSecs")
                xmux.hKeepAlivePeriod = xmuxJson.optLongOrNull("hKeepAlivePeriod")
                extra.xmux = xmux
            }
            val dlJson = extraJson.optJSONObjectOrNull("downloadSettings")
            if (dlJson != null) {
                val dl = Outbound.XHttpConfig.XHttpConfigDownloadSettings()
                dl.address = dlJson.optStringOrNull("address")
                dl.port = dlJson.getInt("port")
                dl.transport = dlJson.optStringOrNull("transport")
                dl.security = dlJson.optStringOrNull("security")
                if (dlJson.has("tlsSettings")) {
                    val tls = dlJson.getJSONObject("tlsSettings")
                    val tlsSettings = Outbound.TlsSettings()
                    tlsSettings.serverName = tls.optStringOrNull("serverName")
                    tlsSettings.verify = !tls.optBoolean("insecure")
                    dl.tlsSettings = tlsSettings
                }
                if (dlJson.has("realitySettings")) {
                    val reality = dlJson.getJSONObject("realitySettings")
                    val realitySettings = Outbound.RealitySettings()
                    realitySettings.serverName = reality.optStringOrNull("serverName")
                    realitySettings.publicKey = reality.optStringOrNull("publicKey")
                    realitySettings.shortId = reality.optStringOrNull("shortId")
                    dl.realitySettings = realitySettings
                }
                if (dlJson.has("xHttpSetting")) {
                    val xHttp = dlJson.getJSONObject("xHttpSetting")
                    val xHttpSettings = Outbound.XHttpConfig()
                    xHttpSettings.host = xHttp.optStringOrNull("host")
                    xHttpSettings.path = xHttp.optStringOrNull("path")
                    xHttpSettings.mode = xHttp.optStringOrNull("mode")
                    xHttpSettings.extra = parseXHttpExtraFromNode(xHttp.optStringOrNull("extra"))
                    dl.xHttpSettings = xHttpSettings
                }
                extra.downloadSettings = dl
            }
            extra
        } catch (e: Exception) {
            App.log("json aaaa ${e}")
            null
        }
    }

    fun getTransportFormQuery(
        node: NodeItem,
        queryParam: Map<String, String>,
    ) {
        node["transport"] = queryParam["type"] ?: TCP.type
        node["header_type"] = queryParam["headerType"]
        node["host"] = queryParam["host"]
        node["path"] = queryParam["path"]

        node["service_name"] = queryParam["serviceName"]
        node["x_http_mode"] = queryParam["mode"]

        node["security"] = queryParam["security"]
        if (node["security"] != AppConfig.TLS && node["security"] != AppConfig.REALITY) {
            node["security"] = null
        }
        // Support multiple possible query keys for allowInsecure like the C# implementation
        val allowInsecureKeys = arrayOf(
            "insecure",
            "allowinsecure",
            "allow_insecure"
        )

        node["insecure"] = when {
            allowInsecureKeys.any { key ->
                queryParam.entries.any { (paramKey, value) ->
                    paramKey.equals(key, ignoreCase = true) && value == "1"
                }
            } -> "1"

            allowInsecureKeys.any { key ->
                queryParam.entries.any { (paramKey, value) ->
                    paramKey.equals(key, ignoreCase = true) && value == "0"
                }
            } -> "0"

            else -> null
        }

        node["sni"] = queryParam["sni"]
        node["alpn"] = queryParam["alpn"]
        node["public_key"] = queryParam["pbk"]
        node["short_id"] = queryParam["sid"]
        node["pcs"] = queryParam["pcs"]
        node["pcn"] = queryParam["pcn"]
        node["ech"] = queryParam["ech"]

        node["extra_json"] = URLDecoder.decode(
            queryParam["extra"].orEmpty(),
            "UTF-8"
        )
    }

    /**
     * Creates a map of query parameters from a NodeItem object.
     *
     * @param config the NodeItem object to create query parameters from
     * @return a map of query parameters
     */
    fun getQueryTransportDic(config: NodeItem): HashMap<String, String> {
        val dicQuery = HashMap<String, String>()
        dicQuery["security"] = config["security"]?.ifEmpty { "none" }.orEmpty()
        config["sni"].let { if (it.isNotNullEmpty()) dicQuery["sni"] = it.orEmpty() }
        config["alpn"].let { if (it.isNotNullEmpty()) dicQuery["alpn"] = it.orEmpty() }
        config["pcs"].let { if (it.isNotNullEmpty()) dicQuery["pcs"] = it.orEmpty() }
        config["pcn"].let { if (it.isNotNullEmpty()) dicQuery["pcn"] = it.orEmpty() }
        config["ech"].let { if (it.isNotNullEmpty()) dicQuery["ech"] = it.orEmpty() }
        config["public_key"].let { if (it.isNotNullEmpty()) dicQuery["pbk"] = it.orEmpty() }
        config["short_id"].let { if (it.isNotNullEmpty()) dicQuery["sid"] = it.orEmpty() }
        config["extra_json"].let {
            if (it.isNotNullEmpty()) {
                dicQuery["extra"] = URLEncoder.encode(it.orEmpty(), "UTF-8")
            }
        }
        // Add two keys for compatibility: "insecure" and "allowInsecure"
        if (config["security"] == AppConfig.TLS) {
            val insecureFlag = if (config["insecure"] == "1") "1" else "0"
            dicQuery["insecure"] = insecureFlag
            dicQuery["allowInsecure"] = insecureFlag
        }

        val networkType = TransportType.fromString(config["transport"])
        dicQuery["type"] = networkType.type
        when (networkType) {
            TCP -> {
                dicQuery["headerType"] = config["header_type"]?.ifEmpty { "none" }.orEmpty()
                config["host"].let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config["path"].let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
            }


            WS, HTTP_UPGRADE -> {
                config["host"].let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config["path"].let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
            }

            XHTTP -> {
                config["host"].let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config["path"].let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
                config["x_http_mode"].let {
                    if (it.isNotNullEmpty()) dicQuery["mode"] = it.orEmpty()
                }
            }

            H2 -> {
                dicQuery["type"] = "http"
                config["host"].let { if (it.isNotNullEmpty()) dicQuery["host"] = it.orEmpty() }
                config["path"].let { if (it.isNotNullEmpty()) dicQuery["path"] = it.orEmpty() }
            }

            GRPC -> {
                config["service_name"].let {
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
        val transport = nodeItem["transport"].orEmpty()
        val headerType = nodeItem["header_type"]
        val host = nodeItem["host"]
        val path = nodeItem["path"]
        val serviceName = nodeItem["service_name"]
        val xhttpMode = nodeItem["x_http_mode"]

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
                val xHttpSettings = Outbound.XHttpConfig()
                xHttpSettings.host = host.orEmpty()
                xHttpSettings.path = path ?: "/"
                xHttpSettings.mode = xhttpMode
                xHttpSettings.extra = parseXHttpExtraFromNode(nodeItem["extra_json"])
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
        val security = nodeItem["security"].orEmpty()
        val allowInsecure = decideAllowInsecure(nodeItem)
        val sni = nodeItem["sni"]

        val alpns = nodeItem["alpn"]
        val alpnsArray = if (alpns.isNullOrEmpty()) null else alpns.split(",").map { it.trim() }
            .filter { it.isNotEmpty() }
        val publicKey = nodeItem["public_key"]
        val shortId = nodeItem["short_id"]

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
            if (nodeItem["pcs"] != null) {
                val pcsArray = nodeItem["pcs"].toString().split(",").map { it.trim() }
                    .filter { it.isNotEmpty() }
                streamSettings.tlsSettings?.pinnedPeerCertSha256 = pcsArray
            }
            if (nodeItem["pcn"] != null) {
                val pcnArray = nodeItem["pcn"].toString().split(",").map { it.trim() }
                    .filter { it.isNotEmpty() }
                streamSettings.tlsSettings?.verifyPeerCertByName = pcnArray
            }
            if (nodeItem["ech"] != null) {
                streamSettings.tlsSettings?.echConfigList = nodeItem["ech"]
            }
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
                nodeItem["insecure"] == "1"
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

package com.xray.core.rust.client.xcra.dto.core

import java.io.UnsupportedEncodingException
import java.net.URLDecoder


class Parser(url: String) {
//    private var uri: URI? = null
//    private var url: String? = null
//
//    init {
//        var url = url
//        try {
//            if (url.indexOf('#') > 0) {
//                url = url.substringBefore('#')
//            }
//            this.url = url
//            uri = URI(url)
//        } catch (ignored: Throwable) {
//        }
//    }

//    fun parse(): Outbound? {
//        if ("direct" == url) {
//            return Outbound(
//                protocol = "freedom"
//            )
//        }
//
//        val scheme = uri?.scheme?.lowercase() ?: return null
//
//        if (scheme
//                .equals("vless", ignoreCase = true)
//        ) {
//            return parseVless()
//        }
//
////        if (scheme
////                .equals("vmess", ignoreCase = true)
////        ) {
////            return parseVmess()
////        }
////        if (scheme
////                .equals("trojan", ignoreCase = true)
////        ) {
////            return parseTrojan()
////        }
////        if (scheme
////                .equals("ss", ignoreCase = true) || scheme
////                .equals("shadowsocks", ignoreCase = true)
////        ) {
////            return parseShadowSocks()
////        }
//        return null
//    }

//    private fun parseVless(): Outbound {
//        val params: MutableMap<String?, String?> = splitQuery(uri?.query)
//
//        var streamSettings: Outbound.StreamSetting = Outbound.StreamSetting()
//        val type = params["type"]
//
//
//        if ("tcp".equals(type, ignoreCase = true)) {
//            streamSettings.transport = "tcp"
//            val headerType = params["headerType"]
//            val path = params["path"]
//            val host = params["host"]
//            if ("http".equals(headerType, ignoreCase = true)) {
//                val headers = hashMapOf<String, String>()
//                host?.let {
//                    headers["Host"] = it
//                }
//                streamSettings.tcpSettings =
//                    Outbound.TcpSettings(type = "http", request = Outbound.TcpRequest(path = path))
//            } else {
//                streamSettings.tcpSettings = Outbound.TcpSettings()
//            }
//        }
//        if ("ws".equals(type, ignoreCase = true)) {
//            streamSettings.transport = "ws"
//            val path = params["path"]
//            val host = params["host"]
//            streamSettings.wsSettings = Outbound.WsSettings(host = host, path = path)
//        }
//
//        if ("httpupgrade".equals(type, ignoreCase = true)) {
//            streamSettings.transport = "http_upgrade"
//            val path = params["path"]
//            val host = params["host"]
//            streamSettings.httpUpgradeSettings =
//                Outbound.HttpUpgradeSettings(host = host, path = path)
//        }
//
////        if ("xhttp".equals(type, ignoreCase = true)) {
////            streamSettings =
////                XHttpSettings(params.get("path"), params.get("host"), params.get("mode"))
////        }
//        if ("grpc".equals(type, ignoreCase = true)) {
//            streamSettings.transport = "grpc"
//            val serviceName = params["serviceName"]
//            streamSettings.grpcSettings = Outbound.GrpcSettings(serviceName = serviceName)
//        }
//
//
//        val security = params["security"]
//        if (security != null) {
//            if (security.equals("tls", ignoreCase = true)) {
//                streamSettings.security = "tls"
//                streamSettings.tlsSettings = Outbound.TlsSettings(serverName = params.get("sni"))
//            }
//            if (security.equals("reality", ignoreCase = true)) {
//                streamSettings.security = "reality"
//                streamSettings.realitySettings = Outbound.RealitySettings(
//                    serverName = params.get("sni"),
//                    publicKey = params.get("pbk"),
//                    shortId = params.get("sid"),
//                )
//            }
//        }
//
//        val vlessSetting =
//            Outbound.VlessSetting(
//                address = uri!!.host,
//                port = uri!!.port,
//                id = uri!!.userInfo,
//            )
//
//        return Outbound(
//            protocol = "vless",
//            settings = vlessSetting,
//            streamSettings = streamSettings
//        )
//    }
//    private fun parseShadowSocks(): Outbound {
//        val params: MutableMap<String?, String?> = splitQuery(uri!!.getQuery())
//
//        var streamSettings: StreamSettings? = null
//        val type = params.get("type")
//        if ("tcp".equals(type, ignoreCase = true)) {
//            val headerType = params.get("headerType")
//            val path = params.get("path")
//            val host = params.get("host")
//            if ("http".equals(headerType, ignoreCase = true)) {
//                streamSettings = TcpSettings(path, host)
//            } else {
//                streamSettings = TcpSettings()
//            }
//        }
//
//        if ("xhttp".equals(type, ignoreCase = true)) {
//            streamSettings =
//                XHttpSettings(params.get("path"), params.get("host"), params.get("mode"))
//        }
//        if ("ws".equals(type, ignoreCase = true)) {
//            streamSettings = WsSettings(params.get("path"), params.get("host"))
//        }
//        if ("httpupgrade".equals(type, ignoreCase = true)) {
//            streamSettings = HttpUpgradeSettings(params.get("path"), params.get("host"))
//        }
//
//        if ("kcp".equals(type, ignoreCase = true)) {
//            val headerType = params.get("headerType")
//            val seed = params.get("seed")
//            streamSettings = KcpSettings(headerType, seed)
//        }
//        if ("grpc".equals(type, ignoreCase = true)) {
//            streamSettings = GrpcSettings(
//                params.get("serviceName"),
//                "multi".equals(params.get("mode"), ignoreCase = true)
//            )
//        }
//        if (streamSettings == null) {
//            streamSettings = TcpSettings()
//        }
//
//        val security = params.get("security")
//        if (security != null) {
//            if (security.equals("tls", ignoreCase = true)) {
//                streamSettings.security = "tls"
//                streamSettings.tlsSettings = TlsSetting(params.get("sni"))
//            }
//            if (security.equals("reality", ignoreCase = true)) {
//                streamSettings.security = "reality"
//                streamSettings.realitySettings = RealitySetting(
//                    params.get("sni"),
//                    params.get("pbk"),
//                    params.get("sid"),
//                    params.get("fp")
//                )
//            }
//        }
//
//        var uot = false
//        var uotVersion = 1
//        if (params.containsKey("uot")) {
//            uot = "1" == params.get("uot")
//        }
//        if (params.containsKey("uotVersion")) {
//            uotVersion = if ("2" == params.get("uotVersion")) 2 else 1
//        }
//        var userInfo = uri.getUserInfo()
//        var method: String? = ""
//        val password = StringBuilder()
//
//        if (!userInfo.contains(":")) {
//            userInfo =
//                kotlin.text.String(org.apache.commons.codec.binary.Base64.decodeBase64(userInfo.toByteArray()))
//        }
//
//        val parts: Array<String?> =
//            userInfo.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        if (parts.size >= 1) {
//            method = parts[0]
//        }
//        if (parts.size >= 2) {
//            for (i in 1..parts.size - 1) {
//                password.append(parts[i])
//                if (i != parts.size - 1) {
//                    password.append(":")
//                }
//            }
//        }
//
//        val trojanSetting: ShadowSocksOutboundSetting = ShadowSocksOutboundSetting(
//            uri.getHost(),
//            uri.getPort(),
//            method,
//            password.toString(),
//            uot,
//            uotVersion
//        )
//
//        return Outbound("shadowsocks", trojanSetting, streamSettings, params.get("fragment"))
//    }
//
//    private fun parseTrojan(): Outbound {
//        val params: MutableMap<String?, String?> = Utility.splitQuery(uri!!.getQuery())
//
//        var streamSettings: StreamSettings? = null
//        val type = params.get("type")
//        if ("tcp".equals(type, ignoreCase = true)) {
//            val headerType = params.get("headerType")
//            val path = params.get("path")
//            val host = params.get("host")
//            if ("http".equals(headerType, ignoreCase = true)) {
//                streamSettings = TcpSettings(path, host)
//            } else {
//                streamSettings = TcpSettings()
//            }
//        }
//
//
//        if ("xhttp".equals(type, ignoreCase = true)) {
//            streamSettings =
//                XHttpSettings(params.get("path"), params.get("host"), params.get("mode"))
//        }
//
//        if ("ws".equals(type, ignoreCase = true)) {
//            streamSettings = WsSettings(params.get("path"), params.get("host"))
//        }
//        if ("httpupgrade".equals(type, ignoreCase = true)) {
//            streamSettings = HttpUpgradeSettings(params.get("path"), params.get("host"))
//        }
//
//        if ("kcp".equals(type, ignoreCase = true)) {
//            val headerType = params.get("headerType")
//            val seed = params.get("seed")
//            streamSettings = KcpSettings(headerType, seed)
//        }
//        if ("grpc".equals(type, ignoreCase = true)) {
//            streamSettings = GrpcSettings(
//                params.get("serviceName"),
//                "multi".equals(params.get("mode"), ignoreCase = true)
//            )
//        }
//        if (streamSettings == null) {
//            streamSettings = TcpSettings()
//        }
//
//        val security = params.get("security")
//        if (security != null) {
//            if (security.equals("tls", ignoreCase = true)) {
//                streamSettings.security = "tls"
//                streamSettings.tlsSettings = TlsSetting(params.get("sni"))
//            }
//            if (security.equals("reality", ignoreCase = true)) {
//                streamSettings.security = "reality"
//                streamSettings.realitySettings = RealitySetting(
//                    params.get("sni"),
//                    params.get("pbk"),
//                    params.get("sid"),
//                    params.get("fp")
//                )
//            }
//        }
//
//        val trojanSetting: TrojanOutboundSetting = TrojanOutboundSetting(
//            uri.getHost(),
//            uri.getPort(),
//            uri.getUserInfo()
//        )
//        return Outbound("trojan", trojanSetting, streamSettings, params.get("fragment"))
//    }
//    private fun parseVmess(): Outbound {
//        val vmess = url!!.replace("vmess://", "")
//        val params: String =
//            kotlin.text.String(org.apache.commons.codec.binary.Base64.decodeBase64(vmess.toByteArray()))
//        val gson = Gson()
//        val vmessInput: VmessInput = gson.fromJson<VmessInput?>(params, VmessInput::class.java)
//        val network: String = vmessInput.net
//        val type: String? = vmessInput.type
//        var streamSettings: StreamSettings? = null
//        if (network.equals("tcp", ignoreCase = true)) {
//            if ("none".equals(type, ignoreCase = true)) {
//                streamSettings = TcpSettings()
//            } else {
//                streamSettings = TcpSettings(vmessInput.path, vmessInput.host)
//            }
//        }
//        if (network.equals("xhttp", ignoreCase = true)) {
//            streamSettings = XHttpSettings(vmessInput.path, vmessInput.host, vmessInput.type)
//        }
//        if (network.equals("ws", ignoreCase = true)) {
//            streamSettings = WsSettings(vmessInput.path, vmessInput.host)
//        }
//        if (network.equals("httpupgrade", ignoreCase = true)) {
//            streamSettings = HttpUpgradeSettings(vmessInput.path, vmessInput.host)
//        }
//        if (network.equals("kcp", ignoreCase = true)) {
//            streamSettings = KcpSettings(vmessInput.type, vmessInput.path)
//        }
//        if (network.equals("grpc", ignoreCase = true)) {
//            streamSettings = GrpcSettings(
//                vmessInput.path,
//                "multi".equals(vmessInput.type, ignoreCase = true)
//            )
//        }
//        if (streamSettings == null) {
//            streamSettings = TcpSettings()
//        }
//
//        if (vmessInput.tls.equals("tls")) {
//            streamSettings.security = "tls"
//            streamSettings.tlsSettings = TlsSetting(vmessInput.sni)
//        }
//
//        val vmessSetting: VmessOutboundSetting = VmessOutboundSetting(
//            vmessInput.add,
//            vmessInput.port.toInt(), vmessInput.id, vmessInput.scy
//        )
//
//        return Outbound("vmess", vmessSetting, streamSettings, vmessInput.fragment)
//    }
}

private fun splitQuery(query: String?): MutableMap<String?, String?> {
    val result: MutableMap<String?, String?> = LinkedHashMap()
    if (query == null) {
        return result
    }
    val pairs = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        try {
            result[URLDecoder.decode(pair.take(idx), "UTF-8")] =
                URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }
    return result
}
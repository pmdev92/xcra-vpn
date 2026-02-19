package com.xray.core.rust.client.xcra.parser


import android.text.TextUtils
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.Vmess
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.enums.TransportType
import com.xray.core.rust.client.xcra.extension.idnHost
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.Base64Util
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI

object ParserVmess : Parser() {

    /**
     * Parses a Vmess string into a NodeItem object.
     *
     * @param str the Vmess string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parse(str: String): NodeItem? {
        if (str.indexOf('?') > 0 && str.indexOf('&') > 0) {
            return parseVmessStd(str)
        }

        val allowInsecure = DatabaseHandler.decodeSettingsBool(AppConfig.PREF_ALLOW_INSECURE, false)
        val config = NodeItem.create(ConfigType.VMESS)

        var result = str.replace(ConfigType.VMESS.getProtocolScheme(), "")
        result = Base64Util.decode(result)
        if (TextUtils.isEmpty(result)) {
            App.log("parse vmess decoding failed")
            return null
        }
        val vmess = JsonUtil.fromJson(result, Vmess::class.java)
        // Although VmessQRCode fields are non null, looks like Gson may still create null fields
        if (TextUtils.isEmpty(vmess.add)
            || TextUtils.isEmpty(vmess.port)
            || TextUtils.isEmpty(vmess.id)
            || TextUtils.isEmpty(vmess.net)
        ) {
            App.log("parse vmess incorrect protocol")
            return null
        }

        config.remarks = vmess.ps
        config.address = vmess.add
        config.port = vmess.port
        config.uuid = vmess.id
        config.vmessSecurity =
            if (TextUtils.isEmpty(vmess.scy)) AppConfig.DEFAULT_VMESS_SECURITY else vmess.scy

        config.transport = vmess.net
        config.headerType = vmess.type
        config.host = vmess.host
        config.path = vmess.path

        when (TransportType.fromString(config.transport)) {
            TransportType.GRPC -> {
                config.serviceName = vmess.path
            }

            else -> {}
        }

        config.security = vmess.tls
        config.sni = vmess.sni
        config.alpn = vmess.alpn
        config.insecure = when (vmess.insecure) {
            "1" -> true
            "0" -> false
            else -> allowInsecure
        }
        return config
    }

    /**
     * Parses a standard Vmess URI string into a NodeItem object.
     *
     * @param str the standard Vmess URI string to parse
     * @return the parsed NodeItem object, or null if parsing fails
     */
    fun parseVmessStd(str: String): NodeItem? {
        val config = NodeItem.create(ConfigType.VMESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)


        config.remarks =
            Utils.urlDecode(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.address = uri.idnHost
        config.port = uri.port.toString()
        config.uuid = uri.userInfo
        config.vmessSecurity = queryParam["security"] ?: "auto"
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
        val vmess = Vmess()
        vmess.v = "2"
        vmess.ps = config.remarks
        vmess.add = config.address.orEmpty()
        vmess.port = config.port.orEmpty()
        vmess.id = config.uuid.orEmpty()
        vmess.scy = config.vmessSecurity.orEmpty()
        vmess.aid = "0"

        vmess.net = config.transport.orEmpty()
        vmess.type = config.headerType.orEmpty()
        when (TransportType.fromString(config.transport)) {
            TransportType.GRPC -> {
                vmess.path = config.serviceName.orEmpty()
            }

            else -> {}
        }
        config.host.let { if (it.isNotNullEmpty()) vmess.host = it.orEmpty() }
        config.path.let { if (it.isNotNullEmpty()) vmess.path = it.orEmpty() }
        vmess.tls = config.security.orEmpty()
        vmess.sni = config.sni.orEmpty()
        vmess.alpn = config.alpn.orEmpty()
        vmess.insecure = when (config.insecure) {
            true -> "1"
            false -> "0"
            else -> ""
        }

        val json = JsonUtil.toJson(vmess)
        return Base64Util.encode(json)
    }

    /**
     * Converts a NodeItem object to an Outbound object.
     *
     * @param nodeItem the NodeItem object to convert
     * @return the converted Outbound object, or null if conversion fails
     */
    fun toOutbound(nodeItem: NodeItem): Outbound? {
        nodeItem.validPort?.let {
            val outbound = Outbound.createInitOutbound(ConfigType.VMESS)
            outbound.settings = Outbound.VmessSetting(
                address = nodeItem.addressConfig,
                port = it,
                id = nodeItem.uuid,
                security = nodeItem.vmessSecurity
            )
            populateTransportSettings(outbound, nodeItem)
            populateSecuritySettings(outbound, nodeItem)
            return outbound
        }
        return null
    }
}
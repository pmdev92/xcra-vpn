package com.xray.core.rust.client.xcra.handler

import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.ConfigResult
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.core.Config
import com.xray.core.rust.client.xcra.dto.core.inbound.Inbound
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.parser.ParserHysteria2
import com.xray.core.rust.client.xcra.parser.ParserShadowSocks
import com.xray.core.rust.client.xcra.parser.ParserSocks5
import com.xray.core.rust.client.xcra.parser.ParserTrojan
import com.xray.core.rust.client.xcra.parser.ParserTuic
import com.xray.core.rust.client.xcra.parser.ParserVless
import com.xray.core.rust.client.xcra.parser.ParserVmess
import com.xray.core.rust.client.xcra.util.IpUtil
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils

object CoreConfigHandler {

    /**
     * Retrieves the connect configuration for the given UUID.
     *
     * @param uuid The unique identifier for the node.
     * @return A ConfigResult object containing the configuration details or indicating failure.
     */
    fun getConfig4Connect(uuid: String): ConfigResult {
        try {
            val nodeItem = DatabaseHandler.decodeNodeItem(uuid) ?: return ConfigResult(false)
            return getConfig4connect(uuid, nodeItem)
        } catch (e: Exception) {
            App.log("Failed to get V2ray config for connect $e")
            return ConfigResult(false)
        }
    }

    /**
     * Retrieves the speedtest configuration for the given UUID.
     *
     * @param uuid The unique identifier for the node.
     * @return A ConfigResult object containing the configuration details or indicating failure.
     */
    fun getConfig4Speedtest(uuid: String): ConfigResult {
        try {
            val nodeItem = DatabaseHandler.decodeNodeItem(uuid) ?: return ConfigResult(false)
            return getConfig4Speedtest(uuid, nodeItem)
        } catch (e: Exception) {
            App.log("Failed to get V2ray config for speedtest $e")
            return ConfigResult(false)
        }
    }

    /**
     * Retrieves the normal V2ray configuration for speedtest.
     *
     * @param uuid The unique identifier for the node item.
     * @param nodeItem The profile item containing the configuration details.
     * @return A ConfigResult object containing the result of the configuration retrieval.
     */
    private fun getConfig4connect(
        uuid: String,
        nodeItem: NodeItem
    ): ConfigResult {
        val result = ConfigResult(false)
        val address = nodeItem.address ?: return result

        if (!IpUtil.isPureIpAddress(address)) {
            if (!IpUtil.isDomainName(address)) {
                App.log("$address is an invalid ip or domain")
                return result
            }
        }
        val config = Config.createTemplate()


        val applyResult = applyInbounds(config, false)
        if (!applyResult.first) {
            return result
        }
        applyOutbound(config, nodeItem) ?: return result
        config.addMoreOutbounds()

        val level = DatabaseHandler.decodeSettingsString(AppConfig.PREF_LOG_LEVEL)
            ?: AppConfig.DEFAULT_LOG_LEVEL
        config.log.level = level

        result.status = true
        result.nodeItem = nodeItem
        result.socksPort = applyResult.second
        result.json = JsonUtil.toJson(config)
        result.uuid = uuid
        return result
    }

    /**
     * Retrieves the configuration for speedtest.
     *
     * @param uuid The unique identifier for the node item.
     * @param nodeItem The profile item containing the configuration details.
     * @return A ConfigResult object containing the result of the configuration retrieval.
     */
    private fun getConfig4Speedtest(
        uuid: String,
        nodeItem: NodeItem
    ): ConfigResult {
        val result = ConfigResult(false)

        val address = nodeItem.address ?: return result
        if (!IpUtil.isPureIpAddress(address)) {
            if (!Utils.isValidUrl(address)) {
                App.log("$address is an invalid ip or domain")
                return result
            }
        }
        val config = Config.createTemplate()


        val applyResult = applyInbounds(config, true)
        if (!applyResult.first) {
            return result
        }
        applyOutbound(config, nodeItem) ?: return result
        config.addMoreOutbounds()

        val level = DatabaseHandler.decodeSettingsString(AppConfig.PREF_LOG_LEVEL) ?: "warning"
        config.log.level = level

        result.status = true
        result.socksPort = applyResult.second
        result.json = JsonUtil.toJson(config)
        result.uuid = uuid
        return result
    }


    /**
     * Configures the primary outbound connection.
     *
     * Converts the node to an outbound configuration and applies global settings.
     *
     * @param config The config configuration object to be modified
     * @param nodeItem The node item containing connection details
     * @return true if outbound configuration was successful, null if there was an error
     */
    private fun applyOutbound(config: Config, nodeItem: NodeItem): Boolean? {
        val outbound = convertNodeItem2Outbound(nodeItem) ?: return null
//        val ret = updateOutboundWithGlobalSettings(outbound)
//        if (!ret) return null

        if (config.outbounds.isNotEmpty()) {
            config.outbounds[0] = outbound
        } else {
            config.outbounds.add(outbound)
        }

//        updateOutboundFragment(v2r1ayConfig)
        return true
    }

    /**
     * Configures the inbound settings for config.
     *
     * This function sets up the inbounds configurations.
     *
     * @param config The configuration object to be modified
     * @return true if inbound configuration was successful, false otherwise
     */
    private fun applyInbounds(config: Config, isForTest: Boolean): Pair<Boolean, Int> {
        try {
            val port = if (isForTest) {
                IpUtil.findFreeTcpPort()
            } else {
                DatabaseHandler.getSocksPort()
            }
            var listen = AppConfig.LOOPBACK_IP
            if (DatabaseHandler.decodeSettingsBool(AppConfig.PREF_PROXY_SHARING)) {
                listen = AppConfig.SHARE_IP
            }
            val inbound = Inbound.createSocks5(listen = listen, port = port)
            config.inbounds.add(inbound)
            return true to port
        } catch (e: Exception) {
            App.log("Failed to configure inbounds $e")

        }
        return false to 0
    }

    /**
     * Converts a node item to an outbound configuration.
     *
     * Creates appropriate outbound settings based on the protocol type.
     *
     * @param nodeItem The profile item to convert
     * @return OutboundBean configuration for the node, or null if not supported
     */
    private fun convertNodeItem2Outbound(nodeItem: NodeItem): Outbound? {
        return when (nodeItem.configType) {
            ConfigType.SOCKS5 -> ParserSocks5.toOutbound(nodeItem)
            ConfigType.VLESS -> ParserVless.toOutbound(nodeItem)
            ConfigType.VMESS -> ParserVmess.toOutbound(nodeItem)
            ConfigType.TROJAN -> ParserTrojan.toOutbound(nodeItem)
            ConfigType.SHADOWSOCKS -> ParserShadowSocks.toOutbound(nodeItem)
            ConfigType.TUIC -> ParserTuic.toOutbound(nodeItem)
            ConfigType.HYSTERIA2 -> ParserHysteria2.toOutbound(nodeItem)
        }
    }
}


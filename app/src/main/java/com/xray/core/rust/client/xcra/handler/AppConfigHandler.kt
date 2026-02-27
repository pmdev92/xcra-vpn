package com.xray.core.rust.client.xcra.handler

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.GroupItem
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.parser.ParserHysteria2
import com.xray.core.rust.client.xcra.parser.ParserShadowSocks
import com.xray.core.rust.client.xcra.parser.ParserSocks5
import com.xray.core.rust.client.xcra.parser.ParserTrojan
import com.xray.core.rust.client.xcra.parser.ParserTuic
import com.xray.core.rust.client.xcra.parser.ParserVless
import com.xray.core.rust.client.xcra.parser.ParserVmess
import com.xray.core.rust.client.xcra.util.Base64Util
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.QRCodeUtil
import com.xray.core.rust.client.xcra.util.Utils
import java.net.URI


object AppConfigHandler {
    /**
     * Shares the configuration to the clipboard.
     *
     * @param context The context.
     * @param uuid The UUID of the configuration.
     * @return The result code.
     */
    fun share2Clipboard(context: Context, uuid: String): Boolean {
        try {
            val conf = createConfigUri(uuid)
            if (!TextUtils.isEmpty(conf)) {
                conf?.let {
                    Utils.setClipboard(context, it)
                    return true
                }
            }
        } catch (e: Exception) {
            App.log("Failed to share config to clipboard $e")
        }
        return false
    }

    /**
     * Shares the configuration as a QR code.
     *
     * @param uuid The UUID of the configuration.
     * @return The QR code bitmap.
     */
    fun share2QRCode(uuid: String): Bitmap? {
        try {
            val conf = createConfigUri(uuid)
            conf?.let {
                return QRCodeUtil.createQRCode(it)
            }
        } catch (e: Exception) {
            App.log("Failed to share config as QR code $e")

        }
        return null
    }


    /**
     * Shares the configuration.
     *
     * @param uuid The UUID of the configuration.
     * @return The configuration string.
     */
    private fun createConfigUri(uuid: String): String? {
        try {
            val config = DatabaseHandler.decodeNodeItem(uuid) ?: return ""

            return config.configType.getProtocolScheme() + when (config.configType) {
                ConfigType.SOCKS5 -> ParserSocks5.toUri(config)
                ConfigType.VLESS -> ParserVless.toUri(config)
                ConfigType.VMESS -> ParserVmess.toUri(config)
                ConfigType.TROJAN -> ParserTrojan.toUri(config)
                ConfigType.SHADOWSOCKS -> ParserShadowSocks.toUri(config)
                ConfigType.TUIC -> ParserTuic.toUri(config)
                ConfigType.HYSTERIA2 -> ParserHysteria2.toUri(config)
            }
        } catch (e: Exception) {
            App.log("Failed to share config for UUID: $uuid $e")
            return null
        }
    }

    /**
     * Imports a batch of configurations.
     *
     * @param str The server string.
     * @param subid The subscription ID.
     * @param append Whether to append the configurations.
     * @return A pair containing the number of configurations and subscriptions imported.
     */
    fun importBatchConfig(str: String?, subid: String, append: Boolean): Pair<Int, Int> {
        var count = parseBatchConfig(Base64Util.decode(str), subid, append)
        if (count <= 0) {
            count = parseBatchConfig(str, subid, append)
        }
//        if (count <= 0) {
//            count = parseCustomConfigServer(server, subid)
//        }
        var countSub = parseBatchSubscription(str)
        if (countSub <= 0) {
            countSub = parseBatchSubscription(Base64Util.decode(str))
        }
        if (countSub > 0) {
            updateConfigViaGroupAll()
        }
        if (count > 0 || countSub > 0) {
            DatabaseHandler.updateSelectedNode()
        }
        return count to countSub
    }

    /**
     * Parses a batch of subscriptions.
     *
     * @param servers The servers string.
     * @return The number of subscriptions parsed.
     */
    private fun parseBatchSubscription(servers: String?): Int {
        try {
            if (servers == null) {
                return 0
            }

            var count = 0
            servers.lines()
                .distinct()
                .forEach { str ->
                    if (Utils.isValidSubscriptionUrl(str)) {
                        count += importUrlAsSubscription(str)
                    }
                }
            return count
        } catch (e: Exception) {
            App.log("Failed to parse batch subscription $e")
        }
        return 0
    }

    /**
     * Parses a batch of configurations.
     *
     * @param servers The servers string.
     * @param groupId The subscription ID.
     * @param append Whether to append the configurations.
     * @return The number of configurations parsed.
     */
    private fun parseBatchConfig(servers: String?, groupId: String, append: Boolean): Int {
        try {
            if (servers == null) {
                return 0
            }
            val removedSelectedNode =
                if (!TextUtils.isEmpty(groupId) && !append) {
                    DatabaseHandler.decodeNodeItem(
                        DatabaseHandler.getSelectNodeUUID().orEmpty()
                    )?.let {
                        if (it.groupId == groupId) {
                            return@let it
                        }
                        return@let null
                    }
                } else {
                    null
                }
            if (!append) {
                DatabaseHandler.removeNodeViaGroupId(groupId)
            }

            var count = 0
            servers
                .lines()
                .distinct()
                .reversed()
                .forEach {
                    val resId = parseConfig(it, groupId, removedSelectedNode)
                    if (resId) {
                        count++
                    }
                }
            return count
        } catch (e: Exception) {
            App.log("Failed to parse batch config $e")
        }
        return 0
    }


    /**
     * Parses the configuration from a QR code or string.
     *
     * @param str The configuration string.
     * @return The result code.
     */
    private fun parseConfig(
        str: String,
        groupId: String,
        removedNode: NodeItem?
    ): Boolean {
        try {
            if (TextUtils.isEmpty(str)) {
                return false
            }

            val config = if (ConfigType.SOCKS5.isProtocolScheme(str)) {
                ParserSocks5.parse(str)
            } else if (ConfigType.VLESS.isProtocolScheme(str)) {
                ParserVless.parse(str)
            } else if (ConfigType.VMESS.isProtocolScheme(str)) {
                ParserVmess.parse(str)
            } else if (ConfigType.TROJAN.isProtocolScheme(str)) {
                ParserTrojan.parse(str)
            } else if (ConfigType.SHADOWSOCKS.isProtocolScheme(str)) {
                ParserShadowSocks.parse(str)
            } else if (ConfigType.TUIC.isProtocolScheme(str)) {
                ParserTuic.parse(str)
            } else if (ConfigType.HYSTERIA2.isProtocolScheme(str)) {
                ParserHysteria2.parse(str)
            } else {
                null
            }


            if (config == null) {
                return false
            }

            config.groupId = groupId
            val uuid = DatabaseHandler.encodeNodeItem("", config, false)

            if (removedNode != null &&
                config.address == removedNode.address && config.port == removedNode.port
            ) {
                DatabaseHandler.setSelectNodeUUID(uuid)
            }
        } catch (e: Exception) {
            App.log("Failed to parse config $e")
            return false
        }
        return true
    }

    /**
     * Updates the configuration via all subscriptions.
     *
     * @return The number of configurations updated.
     */
    fun updateConfigViaGroupAll(): Int {
        var count = 0
        try {
            DatabaseHandler.decodeGroups().forEach {
                count += updateConfigViaGroup(it)
            }
        } catch (e: Exception) {
            App.log("Failed to update config via all subscriptions $e")
            return 0
        }
        if (count > 0) {
            DatabaseHandler.updateSelectedNode()
        }
        return count
    }

    /**
     * Updates the configuration via a subscription.
     *
     * @param it The subscription item.
     * @return The number of configurations updated.
     */
    fun updateConfigViaGroup(it: Pair<String, GroupItem>): Int {
        var count = 0
        try {
            if (TextUtils.isEmpty(it.first)
                || TextUtils.isEmpty(it.second.remarks)
                || TextUtils.isEmpty(it.second.url)
            ) {
                return 0
            }
            if (!it.second.enabled) {
                return 0
            }
            val url = HttpUtil.toIdnUrl(it.second.url)
            if (!Utils.isValidUrl(url)) {
                return 0
            }
            if (!it.second.allowInsecureUrl) {
                if (!Utils.isValidSubscriptionUrl(url)) {
                    return 0
                }
            }


            var configText = try {
                HttpUtil.getUrlContentWithUserAgent(url, 15000)
            } catch (e: Exception) {
                App.log(
                    "Update subscription: proxy not ready or other error $e",
                )
                ""
            }
            if (configText.isEmpty()) {
                configText = try {
                    HttpUtil.getUrlContentWithUserAgent(url)
                } catch (e: Exception) {
                    App.log(
                        "Update subscription: Failed to get URL content with user agent $e"
                    )
                    ""
                }
            }
            if (configText.isEmpty()) {
                return 0
            }
            count = parseConfigViaSub(configText, it.first, false)
        } catch (e: Exception) {
            App.log("Failed to update config via subscription $e")
        }
        if (count > 0) {
            DatabaseHandler.updateSelectedNode()
        }
        return count
    }

    /**
     * Parses the configuration via a subscription.
     *
     * @param server The server string.
     * @param subid The subscription ID.
     * @param append Whether to append the configurations.
     * @return The number of configurations parsed.
     */
    private fun parseConfigViaSub(server: String?, subid: String, append: Boolean): Int {
        var count = parseBatchConfig(Base64Util.decode(server), subid, append)
        if (count <= 0) {
            count = parseBatchConfig(server, subid, append)
        }
        return count
    }

    /**
     * Imports a URL as a subscription.
     *
     * @param url The URL.
     * @return The number of subscriptions imported.
     */
    private fun importUrlAsSubscription(url: String): Int {
        val subscriptions = DatabaseHandler.decodeGroups()
        subscriptions.forEach {
            if (it.second.url == url) {
                return 0
            }
        }
        val uri = URI(Utils.fixIllegalUrl(url))
        val subItem = GroupItem()
        subItem.remarks = uri.fragment ?: Utils.getDateTimeFormated()
        subItem.url = url
        DatabaseHandler.encodeGroup("", subItem)
        return 1
    }
}
package com.xray.core.rust.client.xcra.handler

import com.tencent.mmkv.MMKV
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.GroupItem
import com.xray.core.rust.client.xcra.dto.NodeInfo
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.VpnConfig
import com.xray.core.rust.client.xcra.util.IpUtil
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils

object DatabaseHandler {

    //region private

    private const val ID_MAIN = "MAIN"
    private const val ID_NODE_CONFIG = "NODE_CONFIG"
    private const val ID_NODE_RAW = "NODE_RAW"
    private const val ID_NODE_AFF = "NODE_AFF"
    private const val ID_GROUP_CONFIGS = "GROUP_CONFIG"
    private const val ID_SETTING = "SETTING"


    private const val KEY_SELECTED_NODE = "SELECTED_NODE"
    private const val KEY_APP_NODES = "APP_NODES"

    private const val KEY_SELECTED_GROUP = "SELECTED_GROUP"
    private const val KEY_APP_GROUPS = "APP_GROUPS"


    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val nodeStorage by lazy {
        MMKV.mmkvWithID(
            ID_NODE_CONFIG,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val nodeRawStorage by lazy { MMKV.mmkvWithID(ID_NODE_RAW, MMKV.MULTI_PROCESS_MODE) }
    private val nodeAffStorage by lazy { MMKV.mmkvWithID(ID_NODE_AFF, MMKV.MULTI_PROCESS_MODE) }
    private val groupStorage by lazy {
        MMKV.mmkvWithID(
            ID_GROUP_CONFIGS,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy { MMKV.mmkvWithID(ID_SETTING, MMKV.MULTI_PROCESS_MODE) }

    //endregion

    //region node

    /**
     * Gets the selected node UUID.
     *
     * @return The selected node UUID.
     */
    fun getSelectNodeUUID(): String? {
        return mainStorage.decodeString(KEY_SELECTED_NODE)
    }

    /**
     * Sets the selected node UUID.
     *
     * @param uuid The node UUID.
     */
    fun setSelectNodeUUID(uuid: String) {

        mainStorage.encode(KEY_SELECTED_NODE, uuid)
    }

    /**
     * remove the selected node.
     *
     */
    fun removeSelectedNode() {
        mainStorage.remove(KEY_SELECTED_NODE)
    }

    /**
     * Update selected node list to first item.
     *
     */
    fun updateSelectedNode() {
        if (getSelectNodeUUID().isNullOrBlank()) {
            val nodeList = decodeNodeList()
            if (nodeList.isNotEmpty()) {
                setSelectNodeUUID(nodeList[0])
            }
        }
    }

    /**
     * Encodes the node list.
     *
     * @param nodeList The list of node UUIDs.
     */
    fun encodeNodeList(nodeList: MutableList<String>) {
        mainStorage.encode(KEY_APP_NODES, JsonUtil.toJson(nodeList))
    }

    /**
     * Decodes the node list.
     *
     * @return The list of node UUIDs.
     */
    fun decodeNodeList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_APP_NODES)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            JsonUtil.fromJson(json, Array<String>::class.java).toMutableList()
        }
    }

    /**
     * Decodes the node item.
     *
     * @param uuid The node UUID.
     * @return The node configuration.
     */
    fun decodeNodeItem(uuid: String): NodeItem? {
        if (uuid.isBlank()) {
            return null
        }
        val json = nodeStorage.decodeString(uuid)
        if (json.isNullOrBlank()) {
            return null
        }
        return JsonUtil.fromJson(json, NodeItem::class.java)
    }


    /**
     * Encodes the node configuration.
     *
     * @param uuid The node UUID.
     * @param config The node configuration.
     * @return The node UUID.
     */
    fun encodeNodeItem(uuid: String, config: NodeItem, save: Boolean): String {
        val key = uuid.ifBlank { Utils.getUuid() }
        nodeStorage.encode(key, JsonUtil.toJson(config))
        val nodeList = decodeNodeList()
        if (!nodeList.contains(key)) {
            nodeList.add(0, key)
            encodeNodeList(nodeList)
            if (save && getSelectNodeUUID().isNullOrBlank()) {
                setSelectNodeUUID(key)
            }
        }
        return key
    }


    /**
     * Removes the node configuration.
     *
     * @param uuid The node UUID.
     */
    fun removeNode(uuid: String) {
        if (uuid.isBlank()) {
            return
        }
        if (getSelectNodeUUID() == uuid) {
            removeSelectedNode()
        }
        val nodeList = decodeNodeList()
        nodeList.remove(uuid)
        encodeNodeList(nodeList)
        nodeStorage.remove(uuid)
        nodeAffStorage.remove(uuid)
    }

    /**
     * Removes the node configurations via group ID.
     *
     * @param groupId The group ID.
     */
    fun removeNodeViaGroupId(groupId: String) {
        if (groupId.isBlank()) {
            return
        }
        nodeStorage.allKeys()?.forEach { key ->
            decodeNodeItem(key)?.let { config ->
                if (config.groupId == groupId) {
                    removeNode(key)
                }
            }
        }
    }

    /**
     * Decodes the node affiliation information.
     *
     * @param uuid The node UUID.
     * @return The node affiliation information.
     */
    fun decodeNodeInfo(uuid: String): NodeInfo? {
        if (uuid.isBlank()) {
            return null
        }
        val json = nodeAffStorage.decodeString(uuid)
        if (json.isNullOrBlank()) {
            return null
        }
        return JsonUtil.fromJson(json, NodeInfo::class.java)
    }

    /**
     * Encodes the node test delay in milliseconds.
     *
     * @param uuid The node UUID.
     * @param testResult The test delay in milliseconds.
     */
    fun encodeNodeTestDelayMillis(uuid: String, testResult: Long) {
        if (uuid.isBlank()) {
            return
        }
        val aff = decodeNodeInfo(uuid) ?: NodeInfo()
        aff.result = testResult
        nodeAffStorage.encode(uuid, JsonUtil.toJson(aff))
    }

    /**
     * Clears all test delay results.
     *
     * @param keys The list of node UUIDs.
     */
    fun clearAllNodeInfos(keys: List<String>?) {
        keys?.forEach { key ->
            decodeNodeInfo(key)?.let { aff ->
                aff.result = 0
                nodeAffStorage.encode(key, JsonUtil.toJson(aff))
            }
        }
    }

    /**
     * Removes all node configurations.
     *
     * @return The number of node configurations removed.
     */
    fun removeAllNodes(): Int {
        val count = nodeStorage.allKeys()?.count() ?: 0
        mainStorage.clearAll()
        nodeStorage.clearAll()
        nodeAffStorage.clearAll()
        return count
    }

    /**
     * Removes invalid node configurations.
     *
     * @param uuid The node UUID.
     * @return The number of node configurations removed.
     */
    fun removeInvalidNode(uuid: String): Int {
        var count = 0
        if (uuid.isNotEmpty()) {
            decodeNodeInfo(uuid)?.let { aff ->
                if (aff.result < 0L) {
                    removeNode(uuid)
                    count++
                }
            }
        } else {
            nodeAffStorage.allKeys()?.forEach { key ->
                decodeNodeInfo(key)?.let { aff ->
                    if (aff.result < 0L) {
                        removeNode(key)
                        count++
                    }
                }
            }
        }
        return count
    }

    /**
     * Encodes the raw node configuration.
     *
     * @param uuid The node UUID.
     * @param config The raw node configuration.
     */
    fun encodeNodeRaw(uuid: String, config: String) {
        nodeRawStorage.encode(uuid, config)
    }

    /**
     * Decodes the raw node configuration.
     *
     * @param uuid The node UUID.
     * @return The raw node configuration.
     */
    fun decodeNodeRaw(uuid: String): String? {
        return nodeRawStorage.decodeString(uuid)
    }

    //endregion

    //region Groups


    /**
     * Gets the selected group UUID.
     *
     * @return The selected group UUID.
     */
    fun getSelectGroupUUID(): String? {
        return mainStorage.decodeString(KEY_SELECTED_GROUP)
    }

    /**
     * Sets the selected group UUID.
     *
     * @param uuid The group UUID.
     */
    fun setSelectGroupUUID(uuid: String) {
        mainStorage.encode(KEY_SELECTED_GROUP, uuid)
    }

    /**
     * remove the selected group.
     *
     */
    fun removeSelectedGroup() {
        mainStorage.remove(KEY_SELECTED_GROUP)
    }

    /**
     * Initializes the group list.
     */
    private fun initGroupsList() {
        val groupList = decodeGroupList()
        if (groupList.isNotEmpty()) {
            return
        }
        groupStorage.allKeys()?.forEach { key ->
            groupList.add(key)
        }
        encodeGroupList(groupList)
    }

    /**
     * Decodes the groups.
     *
     * @return The list of groups.
     */
    fun decodeGroups(): List<Pair<String, GroupItem>> {
        initGroupsList()

        val groups = mutableListOf<Pair<String, GroupItem>>()
        decodeGroupList().forEach { key ->
            val json = groupStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                groups.add(Pair(key, JsonUtil.fromJson(json, GroupItem::class.java)))
            }
        }
        return groups
    }

    /**
     * Removes the group.
     *
     * @param uuid The group UUID.
     */
    fun removeGroup(uuid: String) {
        groupStorage.remove(uuid)
        val groupList = decodeGroupList()
        groupList.remove(uuid)
        encodeGroupList(groupList)
        removeNodeViaGroupId(uuid)
    }

    /**
     * Encodes the group.
     *
     * @param uuid The group UUID.
     * @param groupItem The group item.
     */
    fun encodeGroup(uuid: String, groupItem: GroupItem): String {
        val key = uuid.ifBlank { Utils.getUuid() }
        groupStorage.encode(key, JsonUtil.toJson(groupItem))

        val groupsList = decodeGroupList()
        if (!groupsList.contains(key)) {
            groupsList.add(key)
            encodeGroupList(groupsList)
        }
        return key
    }

    /**
     * Decodes the groups.
     *
     * @param groupId The group ID.
     * @return The group item.
     */
    fun decodeGroup(groupId: String): GroupItem? {
        val json = groupStorage.decodeString(groupId) ?: return null
        return JsonUtil.fromJson(json, GroupItem::class.java)
    }

    /**
     * Encodes the group list.
     *
     * @param groupList The list of group IDs.
     */
    fun encodeGroupList(groupList: MutableList<String>) {
        mainStorage.encode(KEY_APP_GROUPS, JsonUtil.toJson(groupList))
    }

    /**
     * Decodes the group list.
     *
     * @return The list of group IDs.
     */
    fun decodeGroupList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_APP_GROUPS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            JsonUtil.fromJson(json, Array<String>::class.java).toMutableList()
        }
    }

    //endregion


    //region Settings

    /**
     * Encodes the settings.
     *
     * @param key The settings key.
     * @param value The settings value.
     * @return Whether the encoding was successful.
     */
    fun encodeSettings(key: String, value: String?): Boolean {
        return settingsStorage.encode(key, value)
    }

    /**
     * Encodes the settings.
     *
     * @param key The settings key.
     * @param value The settings value.
     * @return Whether the encoding was successful.
     */
    fun encodeSettings(key: String, value: Int): Boolean {
        return settingsStorage.encode(key, value)
    }

    /**
     * Encodes the settings.
     *
     * @param key The settings key.
     * @param value The settings value.
     * @return Whether the encoding was successful.
     */
    fun encodeSettings(key: String, value: Boolean): Boolean {
        return settingsStorage.encode(key, value)
    }

    /**
     * Encodes the settings.
     *
     * @param key The settings key.
     * @param value The settings value.
     * @return Whether the encoding was successful.
     */
    fun encodeSettings(key: String, value: MutableSet<String>): Boolean {
        return settingsStorage.encode(key, value)
    }

    /**
     * Decodes the settings string.
     *
     * @param key The settings key.
     * @return The settings value.
     */
    fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    /**
     * Decodes the settings string.
     *
     * @param key The settings key.
     * @param defaultValue The default value.
     * @return The settings value.
     */
    fun decodeSettingsString(key: String, defaultValue: String?): String? {
        return settingsStorage.decodeString(key, defaultValue)
    }

    /**
     * Decodes the settings boolean.
     *
     * @param key The settings key.
     * @return The settings value.
     */
    fun decodeSettingsBool(key: String): Boolean {
        return settingsStorage.decodeBool(key, false)
    }

    /**
     * Decodes the settings boolean.
     *
     * @param key The settings key.
     * @param defaultValue The default value.
     * @return The settings value.
     */
    fun decodeSettingsBool(key: String, defaultValue: Boolean): Boolean {
        return settingsStorage.decodeBool(key, defaultValue)
    }

    /**
     * Decodes the settings string set.
     *
     * @param key The settings key.
     * @return The settings value.
     */
    fun decodeSettingsStringSet(key: String): MutableSet<String>? {
        return settingsStorage.decodeStringSet(key)
    }

    /**
     * Get the SOCKS port.
     * @return The SOCKS port.
     */
    fun getSocksPort(): Int {
        return Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_SOCKS_PORT),
            AppConfig.DEFAULT_PORT_SOCKS.toInt()
        )
    }

    /**
     * Get the HTTP port.
     * @return The HTTP port.
     */
    fun getHttpPort(): Int {
        return getSocksPort() + 1
    }

    /**
     * Retrieves the currently selected VPN interface address configuration.
     * This method reads the user's preference for VPN interface addressing and returns
     * the corresponding configuration containing IPv4 and IPv6 addresses.
     *
     * @return The selected VpnInterfaceAddressConfig instance, or the default configuration
     *         if no valid selection is found or if the stored id is invalid.
     */
    fun getCurrentVpnInterfaceAddressConfig(): VpnConfig {
        val id =
            decodeSettingsString(
                AppConfig.PREF_VPN_INTERFACE_ADDRESS
            ) ?: AppConfig.DEFAULT_VPN_INTERFACE_ADDRESS
        return VpnConfig.getConfigById(id)
    }

    /**
     * Get the VPN MTU from settings, defaulting to AppConfig.VPN_MTU.
     */
    fun getVpnMtu(): Int {
        return Utils.parseInt(
            decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_MTU),
            AppConfig.DEFAULT_VPN_INTERFACE_MTU
        )
    }

    /**
     * Check if routing rulesets bypass LAN.
     * @return True if bypassing LAN, false otherwise.
     */
    fun getVpnBypassLan(): Boolean {
        return decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_BYPASS_LAN)
    }

    /**
     * Check if ipv6 is enable.
     * @return True if ipv6 is enable, false otherwise.
     */
    fun getVpnIpv6Enable(): Boolean {
        return decodeSettingsBool(AppConfig.PREF_VPN_INTERFACE_IPV6)
    }

    /**
     * Get VPN DNS servers from preference.
     * @return A list of VPN DNS servers.
     */
    fun getVpnDnsServers(): List<String> {
        val vpnDns = decodeSettingsString(AppConfig.PREF_VPN_INTERFACE_DNS)
            ?: AppConfig.DEFAULT_VPN_INTERFACE_DNS
        return vpnDns.split(",").filter { IpUtil.isPureIpAddress(it) }
    }
    //endregion
}

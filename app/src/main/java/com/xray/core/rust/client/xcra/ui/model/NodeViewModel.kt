package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.config.ConfigurableItem
import com.xray.core.rust.client.xcra.config.IntegerItem
import com.xray.core.rust.client.xcra.config.Item
import com.xray.core.rust.client.xcra.config.TextItem
import com.xray.core.rust.client.xcra.config.createItemFromKey
import com.xray.core.rust.client.xcra.config.getProtocolKeys
import com.xray.core.rust.client.xcra.config.getSecurityKeys
import com.xray.core.rust.client.xcra.config.getTransportKeys
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils


class NodeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private var isNew: Boolean = true
    private var editUuid: String? = null
    var isShowDelete by mutableStateOf(false)
        private set
    private var groupId: String? = null
    private lateinit var nodeItem: NodeItem
    private val itemsMap = mutableMapOf<String, Item>()

    var items = mutableStateOf<List<Item>>(listOf())
        private set

    constructor(
        application: Application,
        editUuid: String,
        node: NodeItem,
        subscriptionId: String?
    ) : this(application) {
        this.nodeItem = node

        this.editUuid = editUuid
        this.groupId = subscriptionId
        isNew = false
        isShowDelete = true
        initialize()
    }

    constructor(context: Application, configType: ConfigType, subscriptionId: String?) : this(
        context
    ) {
        this.nodeItem = NodeItem.create(configType)
        this.groupId = subscriptionId
        isNew = true
        isShowDelete = false
        initialize()
    }

    private fun initialize() {
        createItems()
        if (!isNew) {

            applyNodeItems()
        }
        validateItems()
    }

    private fun getKeys(): List<String> {
        val currentKeys = mutableListOf<String>()
        currentKeys.addAll(getProtocolKeys(nodeItem))

        if (nodeItem.configType.hasTransport()) {
            currentKeys.addAll(getTransportKeys(nodeItem))
            currentKeys.addAll(getSecurityKeys(nodeItem))
        }

        return currentKeys
    }

    private fun getOrCreateItemFromKey(key: String): Item {
        if (itemsMap.containsKey(key)) {
            return itemsMap[key]!!
        }
        val item = createItemFromKey(app, key)
        itemsMap[key] = item
        return item
    }

    private fun createItems() {
        itemsMap.clear()
        for (key in getKeys()) {
            getOrCreateItemFromKey(key)
        }
    }

    fun validateItems() {
        val current = mutableListOf<Item>()
        val allKeys = getKeys()
        for (key in allKeys) {
            val item = getOrCreateItemFromKey(key)
            current.add(item)
        }
        this@NodeViewModel.items.value = current
    }

    fun updateItemValue(item: ConfigurableItem, value: String) {

        item.updateValue(value)
        val key = item.key
        nodeItem[key] = value
        validateItems()
    }

    fun applyNodeItems() {
        for (key in getKeys()) {
            val value = nodeItem[key] ?: ""
            itemsMap[key]?.let {
                if (it is ConfigurableItem) {
                    it.updateValue(value)
                }
            }
        }
    }

    fun saveNode(): Boolean {
        val addressField = itemsMap["address"]
        if (addressField != null) {
            val addressValue = (addressField as TextItem).getValue().trim()
            if (addressValue.isEmpty()) {
                addressField.setError()
                return false
            }
        }

        val portField = itemsMap["port"] as? IntegerItem
        if (portField != null) {
            val portValue = Utils.parseInt(portField.getValue().trim())
            val portInvalid = portValue !in 1..65534
            if (nodeItem.configType != ConfigType.HYSTERIA2 && portInvalid) {
                portField.setError()
                return false
            }
        }

        when (nodeItem.configType) {
            ConfigType.VLESS, ConfigType.VMESS -> {
                val uuidField = itemsMap["uuid"]
                if (uuidField != null && (uuidField as TextItem).getValue().trim().isEmpty()) {
                    uuidField.setError()
                    return false
                }
            }

            ConfigType.TROJAN, ConfigType.SHADOWSOCKS, ConfigType.HYSTERIA2 -> {
                val pwField = itemsMap["password"]
                if (pwField != null && (pwField as TextItem).getValue().trim().isEmpty()) {
                    pwField.setError()
                    return false
                }
            }

            ConfigType.TUIC -> {
                val uuidField = itemsMap["uuid"]
                if (uuidField != null && (uuidField as TextItem).getValue().trim().isEmpty()) {
                    uuidField.setError()
                    return false
                }
                val pwField = itemsMap["password"]
                if (pwField != null && (pwField as TextItem).getValue().trim().isEmpty()) {
                    pwField.setError()
                    return false
                }
            }

            else -> {}
        }

        if (nodeItem["remarks"].isNullOrEmpty()) {
            nodeItem["remarks"] = Utils.getDateTimeFormated()
        }

        if (nodeItem.groupId.isEmpty() && !groupId.isNullOrEmpty()) {
            nodeItem.groupId = groupId.orEmpty()
        }
        App.log("Saved node is " + JsonUtil.toJsonPretty(nodeItem))
        if (isNew) {
            nodeItem.addedTime = System.currentTimeMillis()
        }

        DatabaseHandler.encodeNodeItem(this.editUuid.orEmpty(), nodeItem, true)
        return true
    }

    fun deleteNode(): Boolean {
        editUuid?.let {
            if (it != DatabaseHandler.getSelectNodeUUID()) {
                DatabaseHandler.removeNode(it)
            } else {
                app.toast(R.string.toast_action_not_allowed)
                return false
            }
        }
        return true
    }
}

private val LocalNodeViewModel = staticCompositionLocalOf<NodeViewModel> {
    error("NodeViewModel not provided")
}

object NodeViewModelAccessor {
    val nodeViewModel: NodeViewModel
        @Composable @ReadOnlyComposable get() = LocalNodeViewModel.current
}

@Composable
fun NodeViewModel(
    nodeViewModel: NodeViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNodeViewModel.provides(nodeViewModel)) {
        content()
    }
}

package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.GroupItem
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.enums.ImportType
import com.xray.core.rust.client.xcra.enums.PingState
import com.xray.core.rust.client.xcra.enums.VpnState
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.AppConfigHandler
import com.xray.core.rust.client.xcra.handler.AppConfigHandler.importBatchConfig
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.service.TestService
import com.xray.core.rust.client.xcra.ui.component.GroupUiItem
import com.xray.core.rust.client.xcra.ui.component.NodeUiItem
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainViewModel(
    application: Application,
    val mainViewModelInterface: MainViewModelInterface
) :
    AndroidViewModel(application) {

    private var activeNodeTestID: String? = null

    var nodePingValue by mutableLongStateOf(-1L)
        private set

    var nodePingState by mutableStateOf(PingState.NOT_MEASURED)
        private set

    var isLoading: MutableState<Boolean> = mutableStateOf(false)
        private set

    var vpnState by mutableStateOf(VpnState.DISCONNECTED)
        private set


    var selectedGroupIndex: MutableState<Int>
        private set
    var selectedGroupUuid: MutableState<String>
        private set

    var selectedNodeUuid: MutableState<String>
        private set

    private var nodeList = DatabaseHandler.decodeNodeList()
    var nodes = mutableStateListOf<NodeUiItem>()
//        private set

    var groups = mutableStateListOf<GroupUiItem>()
        private set
    var filter by mutableStateOf("")
        private set

    init {
        val groupId =
            DatabaseHandler.getSelectGroupUUID().orEmpty()

        val nodeId =
            DatabaseHandler.getSelectNodeUUID().orEmpty()


        this.selectedNodeUuid = mutableStateOf(nodeId)
        this.selectedGroupUuid = mutableStateOf(groupId)
        this.selectedGroupIndex = mutableIntStateOf(0)

        updateGroups()
        updateNodes()
    }


    @Synchronized
    fun updateGroups() {
        groups.clear()
        val list = DatabaseHandler.decodeGroups()
        groups.add(GroupUiItem("", GroupItem(remarks = "All Configs")))
        for (item in list) {
            groups.add(GroupUiItem(item.first, item.second))
        }
        validateSelectedItems()
    }

    @Synchronized
    fun updateNodes() {
        nodes.clear()
        for (uuid in nodeList) {
            val node = DatabaseHandler.decodeNodeItem(uuid) ?: continue
            if (selectedGroupUuid.value.isNotEmpty() && selectedGroupUuid.value != node.groupId) {
                continue
            }
            val info = DatabaseHandler.decodeNodeInfo(uuid)
            if (filter.isEmpty() || node.remarks.lowercase()
                    .contains(filter.lowercase())
            ) {
                nodes.add(NodeUiItem(uuid, node, mutableStateOf(info)))
            }
        }
        validateSelectedItems()
    }

    fun updateNodes(newValue: MutableList<NodeUiItem>) {
        nodes.clear()
        nodes.addAll(newValue)
    }


    @Synchronized
    fun updateFilter(value: String) {
        filter = value
        updateNodes()
    }

    @Synchronized
    fun updateNodeInfo(uuid: String) {
        App.log("updateNodeInfo uuid $uuid")
        for (node in nodes) {
            if (node.uuid == uuid) {
                val info = DatabaseHandler.decodeNodeInfo(uuid)
                node.nodeInfo.value = info
            }
        }
        validateSelectedItems()
    }

    @Synchronized
    fun testAllNodes() {
        TestService.cancelCurrentTests(getApplication())
        nodes.forEach {
            it.nodeInfo.value = null
        }
        val uuids = nodes.map {
            it.uuid
        }.toList()

        DatabaseHandler.clearAllNodeInfos(uuids)
        val nodeCopy = nodes.toList()
        viewModelScope.launch(Dispatchers.Default) {
            for (item in nodeCopy) {
                TestService.testUUID(getApplication(), item.uuid)
            }
        }
    }

    @Synchronized
    fun testActiveNode() {
        if (nodePingState != PingState.MEASURING && vpnState == VpnState.CONNECTED) {
            viewModelScope.launch(Dispatchers.Default) {
                nodePingState = PingState.MEASURING
                val uuid = Utils.getUuid()
                activeNodeTestID = uuid
                TestService.testActive(getApplication(), uuid)
            }
        }
    }


    fun reloadNodes() {
        nodeList = DatabaseHandler.decodeNodeList()
        updateNodes()
    }

    fun deleteAllNodes() {
        DatabaseHandler.removeAllNodes()
        nodes.clear()
    }

    fun deleteNode(context: Context, uuid: String) {
        if (uuid != DatabaseHandler.getSelectNodeUUID()) {
            val pos = nodes.find {
                it.uuid === uuid
            }
            nodes.remove(pos)
            DatabaseHandler.removeNode(uuid)
        } else {
            context.toast(R.string.toast_action_not_allowed)
        }
    }

    fun editNode(uuid: String) {
        mainViewModelInterface.editNode(uuid)
    }

    fun shareNode(uuid: String) {
        mainViewModelInterface.shareNode(uuid)
    }

    fun importBatchConfig(str: String): Pair<Int, Int> {
        return importBatchConfig(
            str,
            selectedGroupUuid.value,
            true
        )
    }

    fun updateConfigViaGroupAll(): Int {
        if (selectedGroupUuid.value.isEmpty()) {
            return AppConfigHandler.updateConfigViaGroupAll()
        } else {
            val groupItem = DatabaseHandler.decodeGroup(selectedGroupUuid.value) ?: return 0
            return AppConfigHandler.updateConfigViaGroup(Pair(selectedGroupUuid.value, groupItem))
        }


    }

    fun addNode(configType: ConfigType) {

        mainViewModelInterface.addNode(configType)
    }

    fun importConfig(importType: ImportType) {
        mainViewModelInterface.importNodes(importType)
    }


    fun requestDeleteAllNodes() {
        mainViewModelInterface.requestDeleteAllNodes()
    }

    fun requestUpdateSubscriptions() {
        mainViewModelInterface.requestUpdateSubscriptions()
    }

    fun updateVpnState(vpnState: VpnState) {
        this.vpnState = vpnState
    }

    private fun updateLoading(value: Boolean) {
        isLoading.value = value
    }

    fun startLoading() {
        updateLoading(true)
    }

    fun stopLoading() {
        updateLoading(false)
    }

    fun setSelectedGroup(uuid: String) {
        if (uuid != this.selectedGroupUuid.value) {
            DatabaseHandler.setSelectGroupUUID(uuid)
            this.selectedGroupUuid.value = uuid
            reloadNodes()
            validateSelectedItems()
        }
    }

    fun setSelectedNode(uuid: String) {
        if (vpnState === VpnState.DISCONNECTING || vpnState === VpnState.CONNECTING) {
            return
        }
        val selected = DatabaseHandler.getSelectNodeUUID()
        if (uuid != selected) {
            DatabaseHandler.setSelectNodeUUID(uuid)
            this.selectedNodeUuid.value = uuid
            if (vpnState === VpnState.CONNECTED) {
                mainViewModelInterface.stopVpn()
                viewModelScope.launch {
                    try {
                        delay(500)
                        mainViewModelInterface.startVpn()
                    } catch (e: Exception) {
                        App.log("Failed to restart V2Ray service $e")
                    }
                }
            }
        }
    }

    fun validateSelectedItems() {
        //validate node
        val nodeId =
            DatabaseHandler.getSelectNodeUUID().orEmpty()

        this.selectedNodeUuid = mutableStateOf(nodeId)

        //validate group
        val groupId =
            DatabaseHandler.getSelectGroupUUID().orEmpty()
        this.selectedGroupUuid = mutableStateOf(groupId)

        //validate group index
        var selectIndex = 0
        val selected = groups.find {
            it.uuid == this.selectedGroupUuid.value
        }
        if (selected != null) {
            selectIndex = groups.indexOf(selected)
        }
        if (selectIndex <= 0) {
            selectIndex = 0
        }
        selectedGroupIndex.value = selectIndex
    }

    fun updateActiveNode(ping: Long) {
        nodePingState = if (ping > 0) {
            PingState.MEASURED_SUCCESS
        } else {
            PingState.MEASURED_TIMEOUT
        }
        this@MainViewModel.nodePingValue = ping
    }

    fun toggleVpn() {
        if (vpnState == VpnState.DISCONNECTED) {
            nodePingState = PingState.NOT_MEASURED
            mainViewModelInterface.toggleVpn()
        }
        if (vpnState == VpnState.CONNECTED) {
            nodePingState = PingState.NOT_MEASURED
            mainViewModelInterface.toggleVpn()
        }
    }

    fun reloadActiveNodeTestResult() {
        activeNodeTestID?.let {
            val result = DatabaseHandler.decodeNodeInfo(it)
            result?.let { nodeInfo ->
                updateActiveNode(nodeInfo.result)
            }
        }
    }

    fun restartVpn() {
        viewModelScope.launch {
            if (vpnState == VpnState.CONNECTED) {
                mainViewModelInterface.stopVpn()
            }
            delay(500)
            mainViewModelInterface.startVpn()
        }
    }


    interface MainViewModelInterface {
        fun addNode(configType: ConfigType)
        fun importNodes(importType: ImportType)
        fun requestDeleteAllNodes()
        fun requestUpdateSubscriptions()
        fun editNode(uuid: String)
        fun shareNode(uuid: String)
        fun toggleVpn()

        fun startVpn()

        fun stopVpn()
    }


}

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModel not provided")
}


object MainViewModelAccessor {
    /**
     * Retrieves the current [MainViewModel] at the call site's position in the hierarchy.
     */
    val mainViewModel: MainViewModel
        @Composable @ReadOnlyComposable get() = LocalMainViewModel.current

}

@Composable
fun MainViewModel(
    mainViewMode: MainViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMainViewModel.provides(mainViewMode)) {
        content()
    }
}
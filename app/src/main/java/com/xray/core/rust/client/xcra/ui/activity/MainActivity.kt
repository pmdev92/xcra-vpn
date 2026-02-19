package com.xray.core.rust.client.xcra.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.enums.DrawerItem
import com.xray.core.rust.client.xcra.enums.ImportType
import com.xray.core.rust.client.xcra.enums.VpnState
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.AppConfigHandler
import com.xray.core.rust.client.xcra.ui.component.AppDrawer
import com.xray.core.rust.client.xcra.ui.component.DeleteAllNodeDialog
import com.xray.core.rust.client.xcra.ui.component.MainTopBar
import com.xray.core.rust.client.xcra.ui.component.QrCodeDialog
import com.xray.core.rust.client.xcra.ui.component.ShareNodeDialog
import com.xray.core.rust.client.xcra.ui.model.MainViewModel
import com.xray.core.rust.client.xcra.ui.screen.MainScreen
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : VpnActivity(), MainViewModel.MainViewModelInterface {
    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        }
    private var showDeleteAllNodeDialog by mutableStateOf(false)
    private var showShareDialog by mutableStateOf(false)
    private var showQrDialog by mutableStateOf(false)
    private var shareUuid: String? = null
    private lateinit var mainViewModel: MainViewModel


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotification()
        mainViewModel = MainViewModel(application, this)
        enableEdgeToEdge()
        setContent {
            MainViewModel(mainViewModel) {
                XcraVPNTheme {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawer { item ->
                                when (item) {
                                    DrawerItem.GroupSettings -> {
                                        val intent = Intent(this, GroupListActivity::class.java)
                                        startActivityForResult.launch(intent)
                                    }

                                    DrawerItem.PerAppProxy -> {
                                        val intent = Intent(this, PerAppActivity::class.java)
                                        startActivityForResult.launch(intent)
                                    }

                                    DrawerItem.Routing -> {
                                        toast("Coming Soon")
                                    }

                                    DrawerItem.UserAsset -> {
                                        toast("Coming Soon")
                                    }

                                    DrawerItem.Settings -> {
                                        val intent = Intent(this, SettingsActivity::class.java)
                                        startActivityForResult.launch(intent)
                                    }

                                    DrawerItem.Logcat -> {
                                        val intent = Intent(this, LogCatActivity::class.java)
                                        startActivity(intent)
                                    }

                                    DrawerItem.CheckForUpdate -> {
                                        val intent = Intent(this, UpdateAppActivity::class.java)
                                        startActivity(intent)
                                    }

                                    DrawerItem.About -> {
                                        val intent = Intent(this, AboutActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                                scope.launch {
                                    drawerState.close()
                                }
                            }

                        }
                    ) {
                        Scaffold(
                            topBar = {
                                MainTopBar(
                                    onMenuClick = {
                                        scope.launch {
                                            if (drawerState.isOpen) {
                                                drawerState.close()
                                            } else {
                                                drawerState.open()
                                            }
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        { innerPadding ->
                            MainScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                            if (showQrDialog && shareUuid != null) {
                                QrCodeDialog(
                                    uuid = shareUuid!!,
                                    onDismiss = { showQrDialog = false }
                                )
                            }
                            if (showShareDialog && shareUuid != null) {
                                ShareNodeDialog(
                                    uuid = shareUuid!!,
                                    onShare2Clipboard = {
                                        share2Clipboard(it)
                                    },
                                    onShowQRCode = {
                                        showQrDialog = true
                                    },
                                    onDismiss = { showShareDialog = false }
                                )
                            }
                            if (showDeleteAllNodeDialog) {
                                DeleteAllNodeDialog(
                                    onDelete = {
                                        deleteAllNode()
                                    },
                                    onDismiss = {
                                        showDeleteAllNodeDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.reloadNodes()
        mainViewModel.reloadActiveNodeTestResult()
    }

    override fun handleVpn(vpnState: VpnState) {
        mainViewModel.updateVpnState(vpnState)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun addNode(configType: ConfigType) {
        val intent = Intent(this, NodeActivity::class.java)
        intent.putExtra(NodeActivity.CREATE_CONFIG_TYPE, configType.value)
            .putExtra(NodeActivity.SUBSCRIPTION_ID, mainViewModel.selectedGroupUuid.value)
        startActivity(intent)
    }

    override fun editNode(uuid: String) {
        val intent = Intent(this, NodeActivity::class.java)
        intent.putExtra(NodeActivity.NODE_UUID, uuid)
            .putExtra(NodeActivity.SUBSCRIPTION_ID, mainViewModel.selectedGroupUuid.value)
        startActivity(intent)
    }

    override fun shareNode(uuid: String) {
        shareUuid = uuid
        showShareDialog = true
    }

    override fun toggleVpn() {
        baseToggleVpn()
    }

    override fun startVpn() {
        baseStartVpn()
    }

    override fun stopVpn() {
        baseStopVpn()
    }

    override fun importNodes(importType: ImportType) {
        when (importType) {
            ImportType.CLIPBOARD -> {
                importClipboard()
            }

            ImportType.QRCODE -> {
                importQrCode()
            }
        }
    }

    override fun requestDeleteAllNodes() {
        showDeleteAllNodeDialog = true
    }

    fun deleteAllNode() {
        mainViewModel.startLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            mainViewModel.deleteAllNodes()
            launch(Dispatchers.Main) {
                mainViewModel.reloadNodes()
                mainViewModel.stopLoading()
                this@MainActivity.toast(this@MainActivity.getString(R.string.toast_del_config_count))
            }
        }
    }

    override fun requestUpdateSubscriptions() {
        mainViewModel.startLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                mainViewModel.updateConfigViaGroupAll()
                launch(Dispatchers.Main) {
                    mainViewModel.reloadNodes()
                    mainViewModel.stopLoading()
                }
            } catch (e: Exception) {
                App.log("Failed to update subscriptions $e")
            }
        }
    }

    private fun importQrCode() {
        openScanner()
    }

    private fun importClipboard() {
        try {
            val clipboard = Utils.getClipboard(this)
            importString(clipboard)
        } catch (e: Exception) {
            App.log("Failed to import config from clipboard $e")
        }
    }

    private fun importString(str: String) {
        mainViewModel.startLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                mainViewModel.importBatchConfig(str)
                launch(Dispatchers.Main) {
                    delay(500L)
                    mainViewModel.reloadNodes()
                    mainViewModel.stopLoading()
                }
            } catch (e: Exception) {
                App.log("Failed to import batch config $e")
            }
        }
    }


    /**
     * Shares server configuration to clipboard
     * @param uuid The server unique identifier
     */
    private fun share2Clipboard(uuid: String) {
        if (AppConfigHandler.share2Clipboard(this, uuid)) {
            this.toastSuccess(R.string.toast_success)
        } else {
            this.toastError(R.string.toast_failure)
        }
    }

    override fun scanResult(text: String?) {
        text?.let {
            importString(text)
        }
    }

    override fun updateNodeItem(uuid: String) {
        mainViewModel.updateNodeInfo(uuid)
    }

    override fun updateActiveNode(ping: Long) {
        mainViewModel.updateActiveNode(ping)
    }
}


package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.enums.ImportType
import com.xray.core.rust.client.xcra.ui.model.MainViewModelAccessor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    onMenuClick: () -> Unit
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    onMenuClick()
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = null
                )
            }
        },
        title = {
            Text(stringResource(R.string.app_name))
        },
        actions = {


            Box {
                IconButton(onClick = {
                    showAddMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                }
                AddConfigMenu(expanded = showAddMenu, onDismiss = {
                    showAddMenu = false
                })
            }

            Box {
                IconButton(onClick = {
                    showMoreMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null
                    )
                }
                MoreMenu(expanded = showMoreMenu, onDismiss = {
                    showMoreMenu = false
                })
            }
        }
    )
}

@Composable
private fun AddConfigMenu(
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    val model = MainViewModelAccessor.mainViewModel
    val callBackCreate = { type: ConfigType ->
        model.addNode(type)
        onDismiss()
    }
    val callBackImport = { type: ImportType ->
        model.importConfig(type)
        onDismiss()
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_import_config_qrcode)) },
            onClick = {
                callBackImport(ImportType.QRCODE)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_import_config_clipboard)) },
            onClick = {
                callBackImport(ImportType.CLIPBOARD)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_socks)) },
            onClick = {
                callBackCreate(ConfigType.SOCKS5)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_vless)) },
            onClick = {
                callBackCreate(ConfigType.VLESS)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_vmess)) },
            onClick = {
                callBackCreate(ConfigType.VMESS)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_trojan)) },
            onClick = {
                callBackCreate(ConfigType.TROJAN)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_shadow_socks)) },
            onClick = {
                callBackCreate(ConfigType.SHADOWSOCKS)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_tuic)) },
            onClick = {
                callBackCreate(ConfigType.TUIC)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_create_hysteria2)) },
            onClick = {
                callBackCreate(ConfigType.HYSTERIA2)
            }
        )
    }
}

@Composable
private fun MoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    val model = MainViewModelAccessor.mainViewModel

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_title_service_restart)) },
            onClick = {
                model.restartVpn()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_test_nodes)) },
            onClick = {
                model.testAllNodes()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_delete_all_configs)) },
            onClick = {
                model.requestDeleteAllNodes()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_sub_update)) },
            onClick = {
                model.requestUpdateSubscriptions()
                onDismiss()
            }
        )
    }
}
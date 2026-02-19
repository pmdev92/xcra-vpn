package com.xray.core.rust.client.xcra.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.component.XcraDividerField
import com.xray.core.rust.client.xcra.ui.component.XcraDropDown
import com.xray.core.rust.client.xcra.ui.component.XcraEditTextField
import com.xray.core.rust.client.xcra.ui.component.XcraTitleTextField
import com.xray.core.rust.client.xcra.ui.model.DividerItem
import com.xray.core.rust.client.xcra.ui.model.DropField
import com.xray.core.rust.client.xcra.ui.model.IntegerField
import com.xray.core.rust.client.xcra.ui.model.NodeViewModel
import com.xray.core.rust.client.xcra.ui.model.NodeViewModelAccessor
import com.xray.core.rust.client.xcra.ui.model.TextField
import com.xray.core.rust.client.xcra.ui.model.TitleItem
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme

class NodeActivity : ComponentActivity() {
    companion object {
        const val CREATE_CONFIG_TYPE: String = "create_config_type"
        const val SUBSCRIPTION_ID: String = "subscription_id"
        const val NODE_UUID: String = "node_uuid"
    }

    private val editUuid by lazy { intent.getStringExtra(NODE_UUID).orEmpty() }
    private val createConfigType by lazy {
        val id = intent.getIntExtra(CREATE_CONFIG_TYPE, ConfigType.VLESS.value)
        ConfigType.fromInt(id) ?: ConfigType.VLESS
    }
    private val subscriptionId by lazy {
        intent.getStringExtra(SUBSCRIPTION_ID)
    }
    private lateinit var nodeViewModel: NodeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isNew = true
        val config = DatabaseHandler.decodeNodeItem(editUuid)
        nodeViewModel = if (config != null) {
            isNew = false
            NodeViewModel(application, editUuid, config, subscriptionId)
        } else {
            NodeViewModel(application, createConfigType, subscriptionId)
        }
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarNode(
                            isNew = isNew,
                            isShowDelete = nodeViewModel.isShowDelete,
                            onBackPress = {
                                finish()
                            },
                            onDeletePress = {
                                if (nodeViewModel.deleteNode()) {
                                    finish()
                                }
                            },
                            onDone = {
                                if (nodeViewModel.saveNode()) {
                                    finish()
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NodeViewModel(
                        nodeViewModel
                    ) {
                        NodeScreen(
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NodeScreen(
    modifier: Modifier = Modifier,
) {
    val model = NodeViewModelAccessor.nodeViewModel
    val fields = model.items.value;

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        fields.forEachIndexed { index, entry ->
            if (entry is DividerItem) {
                XcraDividerField()
            }
            if (entry is TitleItem) {
                XcraTitleTextField(
                    title = entry.title,
                )
            }
            if (entry is TextField) {
                XcraEditTextField(
                    titleResId = entry.titleResId,
                    value = entry.value,
                    onValueChange = { model.updateField(entry, it) },
                    isError = entry.isError,
                )
            }
            if (entry is IntegerField) {
                XcraEditTextField(
                    titleResId = entry.titleResId,
                    value = entry.value,
                    onValueChange = { model.updateField(entry, it) },
                    isError = entry.isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            if (entry is DropField) {
                val items = stringArrayResource(id = entry.itemsResId)
                XcraDropDown(
                    titleResId = entry.titleResId,
                    items = items,
                    selected = entry.value,
                    onValueChange = { model.updateField(entry, it) },
                    isCapitalize = entry.isCapitalize
                )
            }
            if (index != fields.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarNode(
    isNew: Boolean = true,
    isShowDelete: Boolean = false,
    onDeletePress: () -> Unit,
    onBackPress: () -> Unit,
    onDone: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPress()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        title = {
            if (isNew) {
                Text(stringResource(R.string.title_new_node))
            } else {
                Text(stringResource(R.string.title_edit_node))
            }
        },
        actions = {
            if (isShowDelete) {
                IconButton(onClick = {
                    onDeletePress()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
            IconButton(onClick = {
                onDone()
            }) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Done"
                )
            }
        }
    )
}

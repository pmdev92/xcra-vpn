package com.xray.core.rust.client.xcra.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.ui.component.XcraDropDown
import com.xray.core.rust.client.xcra.ui.component.XcraEditTextField
import com.xray.core.rust.client.xcra.ui.component.XcraSwitchField
import com.xray.core.rust.client.xcra.ui.model.RuleViewModel
import com.xray.core.rust.client.xcra.ui.model.RuleViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme

class RuleActivity : ComponentActivity() {


    companion object {
        const val INDEX_ID: String = "asset_uuid"
    }

    private var showDeleteDialog by mutableStateOf(false)

    private val index by lazy { intent.getIntExtra(INDEX_ID, -1) }


    private lateinit var ruleViewModel: RuleViewModel
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        ruleViewModel = RuleViewModel(application, index)
        enableEdgeToEdge()
        setContent {
            RuleViewModel(ruleViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarAsset(
                                onBackPress = {
                                    finish()
                                },
                                onDeletePress = {
                                    if (index > 0) {
                                        showDeleteDialog = true
                                    }
                                },
                                onDone = {
                                    if (ruleViewModel.saveRule()) {
                                        finish()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    { innerPadding ->
                        Screen(
                            modifier = Modifier.padding(innerPadding),
                        )
                        if (showDeleteDialog) {
                            DeleteDialog(
                                onConfirm = {
                                    ruleViewModel.deleteRule()
                                },
                                onDismiss = { showDeleteDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun Screen(
    modifier: Modifier = Modifier,
) {
    val model = RuleViewModelAccessor.ruleViewModel

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        XcraEditTextField(
            titleResId = R.string.rule_lab_remarks,
            value = model.remarks,
            isError = model.remarksError,
            onValueChange = {
                model.updateRemarks(it)
            },
        )

        XcraSwitchField(
            title = stringResource(R.string.rule_lab_locked),
            value = model.locked,
            onValueChange = {
                model.updateLocked(it)
            }
        )

        XcraEditTextField(
            titleResId = R.string.rule_lab_domain,
            value = model.domain,
            isError = model.domainError,
            onValueChange = {
                model.updateDomain(it)
            },
            large = true,
            placeholder = stringResource(R.string.rule_lab_tips)
        )

        XcraEditTextField(
            titleResId = R.string.rule_lab_ip,
            value = model.ip,
            isError = model.ipError,
            onValueChange = {
                model.updateIp(it)
            },
            large = true,
            placeholder = stringResource(R.string.rule_lab_tips)
        )

        XcraEditTextField(
            titleResId = R.string.rule_lab_port,
            value = model.port,
            isError = model.portError,
            onValueChange = {
                model.updatePort(it)
            },
            large = true,
            placeholder = stringResource(R.string.rule_lab_tips)
        )


        var itemsProtocols = stringArrayResource(id = R.array.rule_lab_protocols)
        itemsProtocols = powerSet(itemsProtocols)
        XcraDropDown(
            titleResId = R.string.rule_lab_protocol,
            items = itemsProtocols,
            selected = model.protocol,
            onValueChange = {
                model.updateProtocol(it)
            }
        )

        var itemsNetworks = stringArrayResource(id = R.array.rule_lab_networks)
        itemsNetworks = powerSet(itemsNetworks)
        XcraDropDown(
            titleResId = R.string.rule_lab_network,
            items = itemsNetworks,
            selected = model.network,
            onValueChange = {
                model.updateNetwork(it)
            }
        )

        val itemsOutbounds = stringArrayResource(id = R.array.rule_lab_outbounds)
        XcraDropDown(
            titleResId = R.string.rule_lab_outbound,
            items = itemsOutbounds,
            selected = model.outbound,
            onValueChange = {
                model.updateOutbound(it)
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarAsset(
    onDeletePress: () -> Unit,
    onBackPress: () -> Unit,
    onDone: () -> Unit
) {
    val model = RuleViewModelAccessor.ruleViewModel
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
            Text(stringResource(R.string.title_rule_add))
        },
        actions = {
            if (model.index > 0) {
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

@Composable
private fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        text = {
            Text(text = stringResource(R.string.del_config_confirm))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

fun powerSet(
    list: Array<String>,
    separator: String = " | "
): Array<String> {

    val result = ArrayList<String>()
    val n = list.size
    val max = 1 shl n

    result.add("")

    for (size in 1..n) {
        for (mask in 0 until max) {

            if (Integer.bitCount(mask) == size) {

                val subset = ArrayList<String>()

                for (i in 0 until n) {
                    if ((mask and (1 shl i)) != 0) {
                        subset.add(list[i])
                    }
                }

                result.add(subset.joinToString(separator))
            }
        }
    }

    return result.toTypedArray()
}
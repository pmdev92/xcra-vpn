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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.component.XcraEditTextField
import com.xray.core.rust.client.xcra.ui.component.XcraSwitchField
import com.xray.core.rust.client.xcra.ui.component.XcraTitleTextField
import com.xray.core.rust.client.xcra.ui.model.GroupViewModel
import com.xray.core.rust.client.xcra.ui.model.GroupViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme

class GroupActivity : ComponentActivity() {
    companion object {
        const val GROUP_UUID: String = "node_uuid"
    }

    private val editUuid by lazy { intent.getStringExtra(GROUP_UUID).orEmpty() }


    private lateinit var groupViewModel: GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = DatabaseHandler.decodeGroup(editUuid)
        groupViewModel = if (config != null) {
            GroupViewModel(editUuid, config)
        } else {
            GroupViewModel(editUuid, config)
        }
        enableEdgeToEdge()
        setContent {
            GroupViewModel(groupViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarGroup(
                                isShowDelete = groupViewModel.isShowDelete,
                                onBackPress = {
                                    finish()
                                },
                                onDeletePress = {
                                    if (groupViewModel.deleteGroup()) {
                                        finish()
                                    }
                                },
                                onDone = {
                                    if (groupViewModel.saveGroup()) {
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
    val model = GroupViewModelAccessor.groupViewModel

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        XcraTitleTextField(
            title = stringResource(R.string.group_lab_title)
        )
        XcraEditTextField(
            titleResId = R.string.group_lab_remarks,
            value = model.remarks,
            isError = model.remarksError,
            onValueChange = {
                model.updateRemarks(it)
            },
        )
        XcraEditTextField(
            titleResId = R.string.group_lab_url,
            value = model.url,
            isError = model.urlError,
            onValueChange = {
                model.updateUrl(it)
            },
            large = true
        )
        XcraSwitchField(
            title = stringResource(R.string.group_lab_enable),
            value = model.enabled,
            onValueChange = {
                model.enabled = it
            },
        )
        XcraSwitchField(
            title = stringResource(R.string.group_lab_auto_update),
            value = model.autoUpdate,
            onValueChange = {
                model.autoUpdate = it
            },
        )
        XcraSwitchField(
            title = stringResource(R.string.group_lab_allow_insecure),
            value = model.allowInsecureUrl,
            onValueChange = {
                model.allowInsecureUrl = it
            },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarGroup(
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
            Text(stringResource(R.string.title_new_group))
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
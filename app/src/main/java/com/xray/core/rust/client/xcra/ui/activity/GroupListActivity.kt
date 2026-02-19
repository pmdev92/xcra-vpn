package com.xray.core.rust.client.xcra.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.AppConfigHandler
import com.xray.core.rust.client.xcra.ui.component.GroupItem
import com.xray.core.rust.client.xcra.ui.component.ItemAction
import com.xray.core.rust.client.xcra.ui.model.GroupListViewModel
import com.xray.core.rust.client.xcra.ui.model.GroupListViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GroupListActivity : ComponentActivity(), GroupListViewModel.GroupListModelInterface {
    private lateinit var groupListViewModel: GroupListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            setResult()
        }

        groupListViewModel = GroupListViewModel(application, this)
        enableEdgeToEdge()
        setContent {
            GroupListViewModel(groupListViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarGroupList(
                                onAddPress = {
                                    groupListViewModel.addGroup()
                                },
                                onBackPress = {
                                    setResult()
                                },
                                onUpdatePress = {
                                    groupListViewModel.startLoading()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val count = AppConfigHandler.updateConfigViaGroupAll()
                                        delay(500L)
                                        launch(Dispatchers.Main) {
                                            if (count > 0) {
                                                toastSuccess(R.string.toast_success)
                                            } else {
                                                toastError(R.string.toast_failure)
                                            }
                                            groupListViewModel.stopLoading()
                                        }
                                    }
                                },
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

    override fun onResume() {
        super.onResume()
        groupListViewModel.validateGroups()
    }

    fun setResult() {
        setResult(RESULT_OK, Intent())
        finish()
    }


    override fun addGroup() {
        val intent = Intent(this, GroupActivity::class.java)
        startActivity(intent)
    }

    override fun editGroup(uuid: String) {
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra(GroupActivity.GROUP_UUID, uuid)
        startActivity(intent)
    }

    override fun shareGroup(uuid: String) {

    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
) {
    val model = GroupListViewModelAccessor.groupListViewModel
    val groupList by model.groups.collectAsState()
    val isLoading by model.isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                items(groupList) { item ->
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        GroupItem(
                            groupUiItem = item,
                            onActionClick = { type ->
                                when (type) {
                                    ItemAction.Share -> {
                                        //only share url as node
                                        model.shareGroup(item.uuid)
                                    }

                                    ItemAction.Edit -> {
                                        model.editGroup(item.uuid)
                                    }

                                    ItemAction.Remove -> {
                                        model.removeGroup(item.uuid)
                                    }
                                }
                            },
                            onActionToggle = {
                                model.toggleEnable(item.uuid, it)
                            }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarGroupList(
    onAddPress: () -> Unit,
    onUpdatePress: () -> Unit,
    onBackPress: () -> Unit
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
            Text(stringResource(R.string.title_groups))
        },
        actions = {

            IconButton(onClick = {
                onAddPress()
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
            IconButton(onClick = {
                onUpdatePress()
            }) {
                Icon(
                    imageVector = Icons.Filled.Update,
                    contentDescription = "Update"
                )
            }
        }
    )
}
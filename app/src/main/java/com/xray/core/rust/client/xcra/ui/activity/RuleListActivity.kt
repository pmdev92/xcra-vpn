package com.xray.core.rust.client.xcra.ui.activity


import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.RuleItem
import com.xray.core.rust.client.xcra.enums.PredefinedRulesType
import com.xray.core.rust.client.xcra.enums.RuleActionType
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.RouterHandler
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class RuleListActivity : ScannerActivity() {
    private var showPredefinedDialog by mutableStateOf(false)
    private var showConfirmDialog by mutableStateOf(false)
    private var predefinedRulesType: PredefinedRulesType? = null
    private var ruleAction: RuleActionType? = null
    private var isLoading by mutableStateOf(false)
    private var ruleItemsList by mutableStateOf<List<RuleItem>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarRules(
                            onAddPress = {
                                startActivity(
                                    Intent(this@RuleListActivity, RuleActivity::class.java)
                                )
                            },
                            onActionPress = {
                                when (it) {
                                    RuleActionType.IMPORT_PREDEFINED -> {
                                        ruleAction = it
                                        showPredefinedDialog = true
                                    }

                                    RuleActionType.IMPORT_CLIPBOARD -> {
                                        ruleAction = it
                                        showConfirmDialog = true
                                    }

                                    RuleActionType.IMPORT_QRCODE -> {
                                        ruleAction = it
                                        showConfirmDialog = true
                                    }

                                    RuleActionType.EXPORT_CLIPBOARD -> {
                                        export2Clipboard()
                                    }
                                }
                            },
                            onBackPress = {
                                finish()
                            },
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
                { innerPadding ->
                    Screen(
                        modifier = Modifier.padding(innerPadding),
                        ruleItems = ruleItemsList,
                        onItemSwap = { from, to ->
                            ruleItemsList = ruleItemsList.toMutableList().apply {
                                add(to.index, removeAt(from.index))
                            }
                            RouterHandler.saveRules(ruleItemsList.toMutableList())
                        },
                        onEditClick = {
                            startActivity(
                                Intent(this@RuleListActivity, RuleActivity::class.java)
                                    .putExtra(RuleActivity.INDEX_ID, it)
                            )
                        },
                        onCheckedChange = { index, rule, value ->
                            val updatedRule = rule.copy(enabled = value)
                            RouterHandler.saveRule(index, updatedRule)
                        },
                        isLoading = isLoading
                    )
                    if (showPredefinedDialog) {
                        ChoosePredefinedDialog(
                            onSelected = {
                                predefinedRulesType = it
                                showConfirmDialog = true
                            },
                            onDismiss = { showPredefinedDialog = false }
                        )
                    }
                    if (showConfirmDialog) {
                        ConfirmImportDialog(
                            onConfirm = {
                                importRules()
                            },
                            onDismiss = { showConfirmDialog = false },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ruleItemsList = RouterHandler.getRules()
    }

    private fun importRules() {
        when (ruleAction) {
            RuleActionType.IMPORT_PREDEFINED -> {
                importFromFile(predefinedRulesType)
            }

            RuleActionType.IMPORT_CLIPBOARD -> {
                importFromClipboard()
            }

            RuleActionType.IMPORT_QRCODE -> {
                openScanner()
            }

            else -> {}
        }
    }

    private fun importFromFile(predefinedType: PredefinedRulesType?) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (predefinedType == null) {
                withContext(Dispatchers.Main) {
                    toastError(R.string.toast_failure)
                }
                return@launch
            }
            val fileName = predefinedType.fileName
            val assets = Utils.readTextFromAssets(this@RuleListActivity, fileName)
            if (assets.isEmpty()) {
                withContext(Dispatchers.Main) {
                    toastError(R.string.toast_failure)

                }
                return@launch
            }
            val result = RouterHandler.resetRules(assets)
            withContext(Dispatchers.Main) {
                if (result) {
                    ruleItemsList = RouterHandler.getRules()
                    toastSuccess(R.string.toast_success)
                } else {
                    toastError(R.string.toast_failure)
                }
            }
        }
    }

    private fun importFromClipboard() {
        val clipboard = try {
            Utils.getClipboard(this)
        } catch (e: Exception) {
            App.log("Failed to get clipboard content $e")
            toastError(R.string.toast_failure)
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val result = RouterHandler.resetRules(clipboard)
            withContext(Dispatchers.Main) {
                if (result) {
                    ruleItemsList = RouterHandler.getRules()
                    toastSuccess(R.string.toast_success)
                } else {
                    toastError(R.string.toast_failure)
                }
            }
        }
    }

    override fun scanResult(text: String?) {
        if (text == null) {
            toastError(R.string.toast_failure)
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val result = RouterHandler.resetRules(text)
            withContext(Dispatchers.Main) {
                if (result) {
                    ruleItemsList = RouterHandler.getRules()
                    toastSuccess(R.string.toast_success)
                } else {
                    toastError(R.string.toast_failure)
                }
            }
        }
    }

    private fun export2Clipboard() {
        val rulesetList = DatabaseHandler.decodeRoutingRules()
        if (rulesetList.isNullOrEmpty()) {
            toastError(R.string.toast_failure)
        } else {
            Utils.setClipboard(this, JsonUtil.toJson(rulesetList))
            toastSuccess(R.string.toast_success)
        }
    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    ruleItems: List<RuleItem>,
    onItemSwap: (LazyListItemInfo, LazyListItemInfo) -> Unit,
    onEditClick: (Int) -> Unit,
    onCheckedChange: (Int, RuleItem, Boolean) -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val hapticFeedback = LocalHapticFeedback.current
        val lazyListState = rememberLazyListState()
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                onItemSwap(from, to)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)

        ) {
            itemsIndexed(ruleItems, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                    val color =
                        animateColorAsState(if (isDragging) Color.Gray.copy(alpha = 0.2f) else Color.Transparent)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color.value)
                            .draggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                                dragGestureDetector = DragGestureDetector.LongPress
                            )
                            .padding(horizontal = 4.dp)
                    ) {
                        RuleItemRow(
                            index,
                            item,
                            onEditClick = onEditClick,
                            onCheckedChange = onCheckedChange,
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(4.dp)
                )
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
private fun TopBarRules(
    onAddPress: () -> Unit,
    onActionPress: (RuleActionType) -> Unit,
    onBackPress: () -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }
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
            Text(stringResource(R.string.title_rules))
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

            Box {
                IconButton(onClick = {
                    showMoreMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More"
                    )
                }
                MoreMenu(
                    expanded = showMoreMenu,
                    onActionPress = onActionPress,
                    onDismiss = {
                        showMoreMenu = false
                    })
            }
        }
    )
}


@Composable
private fun MoreMenu(
    expanded: Boolean,
    onActionPress: (RuleActionType) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rules_import_predefined)) },
            onClick = {
                onActionPress(RuleActionType.IMPORT_PREDEFINED)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rules_export_to_clipboard)) },
            onClick = {
                onActionPress(RuleActionType.IMPORT_CLIPBOARD)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rules_import_from_qrcode)) },
            onClick = {
                onActionPress(RuleActionType.IMPORT_QRCODE)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rules_export_to_clipboard)) },
            onClick = {
                onActionPress(RuleActionType.EXPORT_CLIPBOARD)
                onDismiss()
            }
        )
    }
}

@Composable
fun RuleItemRow(
    index: Int,
    ruleItem: RuleItem,
    onEditClick: (Int) -> Unit,
    onCheckedChange: (Int, RuleItem, Boolean) -> Unit,
) {
    val description = (ruleItem.domain ?: ruleItem.ip ?: ruleItem.port)?.toString().orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = ruleItem.remarks.orEmpty(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (ruleItem.locked == true) {
                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ruleItem.outboundTag,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {

                Box(
                    modifier = Modifier
                        .clickable(
                            indication = ripple(bounded = false),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onEditClick(index) }
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                var checked by remember(ruleItem.id) { mutableStateOf(ruleItem.enabled) }
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        onCheckedChange(index, ruleItem, it)
                    }
                )
            }
        }
    }
}

@Composable
fun ChoosePredefinedDialog(
    onSelected: (PredefinedRulesType) -> Unit,
    onDismiss: () -> Unit
) {
    val sources = PredefinedRulesType.entries.toTypedArray()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        text = {
            Column {
                sources.forEach { value ->
                    Text(
                        text = value.displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(value)
                                onDismiss()
                            }
                            .padding(horizontal = 4.dp)
                            .padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
fun ConfirmImportDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(stringResource(R.string.rules_import_confirm))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
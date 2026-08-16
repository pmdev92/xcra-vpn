package com.xray.core.rust.client.xcra.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.LogEntry
import com.xray.core.rust.client.xcra.service.LogService
import com.xray.core.rust.client.xcra.ui.component.LogItem
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogCatActivity : ComponentActivity() {
    private var search by mutableStateOf("")
    private val listState = LazyListState()
    private var logs: MutableList<LogEntry> = mutableStateListOf()
    private var isLoading by mutableStateOf(false)
    private var isEnd by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        loadLogs(clear = false)
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarLogs(
                            onDeletePress = {
                                deleteAll()
                            },
                            onRefreshPress = {
                                refreshAll()
                            },
                            onCopyPress = {
                                copy2Clipboard()
                            },
                            onBackPress = {
                                finish()
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
                { innerPadding ->
                    Screen(
                        modifier = Modifier.padding(innerPadding),
                        search,
                        updateSearch = {
                            search = it
                            loadLogs(clear = true)
                        },
                        listState = listState,
                        isLoading = isLoading,
                        isEnded = isEnd,
                        items = logs,
                        loadMore = {
                            loadLogs(logs.getOrNull(logs.lastIndex), false)
                        },
                        onCopyPress = {
                            copy2Clipboard(it)
                        }
                    )
                }
            }
        }
    }


    fun loadLogs(logEntry: LogEntry? = null, clear: Boolean) {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch(Dispatchers.IO) {
            val loadedLogs = LogService.getLogs(logEntry, search = search)
            if (clear) {
                logs.clear()
            }
            logs.addAll(loadedLogs.second)
            launch(Dispatchers.Main) {
                isLoading = false
                isEnd = loadedLogs.first
            }
        }
    }

    fun deleteAll() {
        LogService.deleteAll()
        lifecycleScope.launch(Dispatchers.Main) {
            logs.clear()
            isLoading = false
            isEnd = false
        }
    }

    fun refreshAll() {
        lifecycleScope.launch(Dispatchers.Main) {
            listState.scrollToItem(0)
            loadLogs(clear = true)
        }
    }

    private fun copy2Clipboard(item: LogEntry) {
        Utils.setClipboard(this, item.display())
    }

    private fun copy2Clipboard() {
        var str = ""
        for (item in logs) {
            str = str + item.display() + "\n"
        }
        Utils.setClipboard(this, str)
    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    search: String,
    updateSearch: (String) -> Unit,
    listState: LazyListState,
    isLoading: Boolean,
    isEnded: Boolean,
    items: List<LogEntry>,
    loadMore: () -> Unit,
    onCopyPress: (LogEntry) -> Unit,
) {
    val density = LocalDensity.current
    var searchHeightDp by remember { mutableStateOf(0.dp) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState, isEnded) {
        if (isEnded) {
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .collect { lastVisible ->
                if (!isEnded && lastVisible != null) {
                    if (lastVisible > items.lastIndex - 20) {
                        loadMore()
                    }
                }
            }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentPadding = PaddingValues(
                    bottom = searchHeightDp
                )
            ) {
                items(items) { item ->
                    LogItem(
                        content = item.display(),
                        onClick = { onCopyPress(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    )
                }
            }

            if (items.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Log cat is empty",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        with(density) {
                            searchHeightDp = size.height.toDp()
                        }
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    TextField(
                        value = search,
                        onValueChange = updateSearch,
                        placeholder = { Text("Search...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(
                                    onClick = { updateSearch("") }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        )
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarLogs(
    onCopyPress: () -> Unit,
    onRefreshPress: () -> Unit,
    onDeletePress: () -> Unit,
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
            Text(stringResource(R.string.title_log_cat))
        },
        actions = {
            IconButton(onClick = {
                onDeletePress()
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }
            IconButton(onClick = {
                onRefreshPress()
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
            IconButton(onClick = {
                onCopyPress()
            }) {
                Icon(
                    imageVector = Icons.Filled.CopyAll,
                    contentDescription = "Copy"
                )
            }
        }
    )
}


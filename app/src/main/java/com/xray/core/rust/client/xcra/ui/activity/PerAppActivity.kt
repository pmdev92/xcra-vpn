package com.xray.core.rust.client.xcra.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.component.ItemApp
import com.xray.core.rust.client.xcra.ui.model.PerAppViewModel
import com.xray.core.rust.client.xcra.ui.model.PerAppViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.Utils

class PerAppActivity : ComponentActivity() {
    private lateinit var perAppViewModel: PerAppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            setResult()
        }

        perAppViewModel = PerAppViewModel(application)
        enableEdgeToEdge()
        setContent {
            PerAppViewModel(perAppViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarPerApp(
                                onMorItemClick = {
                                    when (it) {
                                        MoreAction.Toggle -> {
                                            toggleAll()
                                        }

                                        MoreAction.Import -> {
                                            importFromClipboard()
                                        }

                                        MoreAction.Export -> {
                                            exportToClipboard()
                                        }
                                    }
                                },
                                onBackPress = {
                                    setResult()
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

    override fun onPause() {
        super.onPause()
        val items: MutableSet<String> = mutableSetOf()

        for (app in perAppViewModel.apps.value) {
            if (app.isSelected) {
                items.add(app.packageName)
            }
        }
        DatabaseHandler.encodeSettings(
            AppConfig.PREF_PER_APP_PROXY_APPS_SET,
            items
        )
    }

    fun setResult() {
        setResult(RESULT_OK, Intent())
        finish()
    }

    private fun toggleAll() {
        perAppViewModel.toggleAll()
    }

    private fun importFromClipboard() {
        val str = Utils.getClipboard(applicationContext)
        if (str.isEmpty()) return
        if (perAppViewModel.import(str)) {
            toastSuccess(R.string.toast_success)
        } else {
            toastError(R.string.toast_failure)
        }
    }

    private fun exportToClipboard() {
        val str = perAppViewModel.export()
        Utils.setClipboard(applicationContext, str)
        toastSuccess(R.string.toast_success)
    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
) {
    val model = PerAppViewModelAccessor.perAppViewModel
    val isLoading by model.isLoading
    val apps by model.apps.collectAsState()
    val density = LocalDensity.current
    var searchHeightDp by remember { mutableStateOf(0.dp) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            PerAppProxySettings(
                perAppProxyEnabled = model.perAppProxyEnabled.value,
                onPerAppProxyChange = {
                    model.setPerAppProxyEnable(it)
                },
                bypassAppsEnabled = model.bypassAppsEnabled.value,
                onBypassAppsChange = {
                    model.setBypassAppsEnable(it)
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = searchHeightDp
                    )
                ) {
                    items(apps) { item ->
                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            ItemApp(
                                appInfo = item,
                                onClick = {
                                    model.toggleItem(item)
                                }
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(4.dp)
                        )
                    }
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
                            value = model.filter,
                            onValueChange = {
                                model.updateFilter(it)
                            },
                            placeholder = { Text("Search...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (model.filter.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            model.updateFilter("")
                                        }
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
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }


}


@Composable
fun PerAppProxySettings(
    perAppProxyEnabled: Boolean,
    onPerAppProxyChange: (Boolean) -> Unit,
    bypassAppsEnabled: Boolean,
    onBypassAppsChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.per_app_proxy_settings_enable),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = perAppProxyEnabled,
                onCheckedChange = onPerAppProxyChange,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.switch_bypass_apps_mode),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = bypassAppsEnabled,
                onCheckedChange = onBypassAppsChange
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarPerApp(
    onMorItemClick: (MoreAction) -> Unit,
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
            Text(stringResource(R.string.title_per_app))

        },
        actions = {
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
                    onMorItemClick = onMorItemClick,
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
    onMorItemClick: (MoreAction) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_select_all)) },
            onClick = {
                onMorItemClick(MoreAction.Toggle)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_import_proxy_app)) },
            onClick = {
                onMorItemClick(MoreAction.Import)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_item_export_proxy_app)) },
            onClick = {
                onMorItemClick(MoreAction.Export)
                onDismiss()
            }
        )
    }
}

enum class MoreAction {
    Toggle,
    Import,
    Export,
}

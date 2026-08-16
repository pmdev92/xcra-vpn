package com.xray.core.rust.client.xcra.ui.activity

import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.CheckUpdateResult
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.AppUpdateHandler
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.screen.UpdateScreen
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.launch

class UpdateAppActivity : ComponentActivity() {
    private var isPreReleaseChecked by mutableStateOf(false)
    private var releaseNote by mutableStateOf("")
    private var downloadUrl by mutableStateOf("")
    private var latestVersion by mutableStateOf("")
    private var showDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPreReleaseChecked =
            DatabaseHandler.decodeSettingsBool(AppConfig.PREF_IS_PRE_RELEASE_ENABLE)
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarSettings(
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
                        isPreReleaseChecked = isPreReleaseChecked,
                        onPreReleaseCheckedChange = { value ->
                            isPreReleaseChecked = value
                            DatabaseHandler.encodeSettings(
                                AppConfig.PREF_IS_PRE_RELEASE_ENABLE,
                                value
                            )
                        },
                        onCheckUpdateClick = {
                            checkForUpdates(isPreReleaseChecked)
                        }
                    )
                    if (showDialog) {
                        UpdateDialog(
                            onDismiss = {
                                showDialog = false
                            },
                            latestVersion = latestVersion,
                            releaseNotes = releaseNote,
                            onDownload = {
                                if (URLUtil.isValidUrl(downloadUrl)) {
                                    Utils.openUri(this, downloadUrl)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkForUpdates(includePreRelease: Boolean) {
        toast(R.string.update_checking_for_update)

        lifecycleScope.launch {
            try {
                val result = AppUpdateHandler.checkForUpdate(includePreRelease)
                if (result.hasUpdate) {
                    showUpdateDialog(result)
                } else {
                    toastSuccess(R.string.update_already_latest_version)
                }
            } catch (e: Exception) {
                App.log("Failed to check for updates: $e")
                toastError(e.message ?: getString(R.string.toast_failure))
            }
        }
    }

    private fun showUpdateDialog(result: CheckUpdateResult) {
        releaseNote = result.releaseNotes.orEmpty()
        downloadUrl = result.downloadUrl.orEmpty()
        latestVersion = result.latestVersion.orEmpty()
        showDialog = true
    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    isPreReleaseChecked: Boolean,
    onPreReleaseCheckedChange: (Boolean) -> Unit,
    onCheckUpdateClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        UpdateScreen(
            isPreReleaseChecked = isPreReleaseChecked,
            onPreReleaseCheckedChange = onPreReleaseCheckedChange,
            onCheckUpdateClick = onCheckUpdateClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarSettings(
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
            Text(stringResource(R.string.update_check_for_update))
        },
    )
}

@Composable
fun UpdateDialog(
    latestVersion: String,
    releaseNotes: String,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "New version $latestVersion found")
        },
        text = {
            Text(text = releaseNotes)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDownload()
                    onDismiss()
                }
            ) {
                Text(text = "Update Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
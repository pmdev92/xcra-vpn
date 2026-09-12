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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.component.XcraEditTextField
import com.xray.core.rust.client.xcra.ui.model.AssetViewModel
import com.xray.core.rust.client.xcra.ui.model.AssetViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import java.io.File

class AssetActivity : ComponentActivity() {


    companion object {
        const val ASSET_UUID: String = "asset_uuid"
        const val ASSET_URL_QRCODE: String = "asset_url_qrcode"
    }

    private var showDeleteDialog by mutableStateOf(false)
    private val editUuid by lazy { intent.getStringExtra(ASSET_UUID).orEmpty() }
    private val urlQrCode by lazy { intent.getStringExtra(ASSET_URL_QRCODE).orEmpty() }

    private lateinit var assetViewModel: AssetViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val asset = DatabaseHandler.decodeAsset(editUuid)
        assetViewModel = AssetViewModel(application, editUuid, asset)
        if (urlQrCode.isNotNullEmpty()) {
            assetViewModel.updateUrl(urlQrCode)
            assetViewModel.updateRemarks(File(urlQrCode).name)
        }
        enableEdgeToEdge()
        setContent {
            AssetViewModel(assetViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarAsset(
                                onBackPress = {
                                    finish()
                                },
                                onDeletePress = {
                                    showDeleteDialog = true
                                },
                                onDone = {
                                    if (assetViewModel.saveAssets()) {
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
                                    assetViewModel.deleteAsset()
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
    val model = AssetViewModelAccessor.assetViewModel

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        XcraEditTextField(
            title = stringResource(R.string.asset_lab_remarks),
            value = model.remarks,
            isError = model.remarksError,
            onValueChange = {
                model.updateRemarks(it)
            },
        )
        XcraEditTextField(
            title = stringResource(R.string.asset_lab_url),
            value = model.url,
            isError = model.urlError,
            onValueChange = {
                model.updateUrl(it)
            },
            large = true
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
    val model = AssetViewModelAccessor.assetViewModel
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
            Text(stringResource(R.string.title_asset_add))
        },
        actions = {
            if (!model.uuid.isNullOrEmpty()) {
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
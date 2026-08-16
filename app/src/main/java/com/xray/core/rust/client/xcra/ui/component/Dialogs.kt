package com.xray.core.rust.client.xcra.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.handler.AppConfigHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@Composable
fun DeleteAllNodeDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(stringResource(R.string.dialog_del_config_confirm))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete()
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

@Composable
fun ShareNodeDialog(
    uuid: String,
    onShowQRCode: (uuid: String) -> Unit,
    onShare2Clipboard: (uuid: String) -> Unit,
    onDismiss: () -> Unit
) {

    val shareOptions = stringArrayResource(R.array.share_node_method)
    AlertDialog(
        shape = MaterialTheme.shapes.small,
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Share Node"
                )
            }

        },
        confirmButton = {

        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                shareOptions.forEachIndexed { index, option ->
                    TextButton(
                        onClick = {
                            try {
                                when (index) {
                                    0 -> onShowQRCode(uuid)
                                    1 -> onShare2Clipboard(uuid)
                                }
                            } catch (e: Exception) {
                                App.log("Error when sharing server $e")
                            }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            option,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun QrCodeDialog(
    uuid: String,
    onDismiss: () -> Unit
) {

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uuid) {
        isLoading = true
        val result = withContext(Dispatchers.Default) {
            AppConfigHandler.share2QRCode(uuid)
        }
        delay(500)
        if (result == null) {
            onDismiss()
        } else {
            bitmap = result
        }
        isLoading = false
    }

    if (bitmap != null || isLoading) {
        AlertDialog(
            shape = MaterialTheme.shapes.small,
            onDismissRequest = onDismiss,
            confirmButton = {
                if (bitmap != null) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            },
            text = {
                if (isLoading) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.size(336.dp)
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generating QR Code")
                    }
                } else bitmap?.let {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "QR Code")
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = stringResource(R.string.content_description_qr_code_of_node),
                            modifier = Modifier.size(336.dp)
                        )
                    }
                }
            }
        )
    }
}
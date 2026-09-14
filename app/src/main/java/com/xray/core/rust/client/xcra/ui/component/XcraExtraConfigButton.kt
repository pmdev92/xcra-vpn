package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun XcraExtraConfigButton(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    fun format(s: String): String {
        val formatted = try {
            org.json.JSONObject(s).toString(4)
        } catch (_: Exception) {
            s
        }
        return formatted
    }

    fun isValidJson(s: String): Boolean {
        return try {
            org.json.JSONObject(s)
            true
        } catch (_: Exception) {
            false
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var text by remember {
        mutableStateOf(value.let {
            format(it)
        })
    }
    var showError by remember { mutableStateOf(false) }




    Button(
        onClick = {
            text = value
            showError = false
            showDialog = true
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = MaterialTheme.shapes.extraSmall,
            title = {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                    TextField(
                        value = text,
                        onValueChange = {
                            val formatted = format(it)
                            text = formatted
                            showError = !isValidJson(formatted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        isError = showError,
                        keyboardOptions = KeyboardOptions(),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    if (showError && errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValidJson(text)) {
                            onValueChange(text)
                            showError = false
                            showDialog = false
                        } else {
                            showError = true
                        }
                    },
                    enabled = isValidJson(text),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                ) { Text("Save") }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                ) { Text("Cancel") }
            }
        )
    }
}

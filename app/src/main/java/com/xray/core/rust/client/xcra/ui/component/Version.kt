package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.xrayVersion

@Composable
fun VersionInformation(
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var xrayVersionText by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            xrayVersion(object : com.xray.core.rust.Version {
                override fun version(version: String) {
                    xrayVersionText = version
                }
            })
        }
        Text(
            "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            "Xray Core Rust: $xrayVersionText",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            "App ID: ${BuildConfig.APPLICATION_ID}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
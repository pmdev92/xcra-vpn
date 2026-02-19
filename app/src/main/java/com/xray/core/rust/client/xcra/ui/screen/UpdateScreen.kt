package com.xray.core.rust.client.xcra.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.BuildConfig

@Composable
fun UpdateScreen(
    isPreReleaseChecked: Boolean,
    onPreReleaseCheckedChange: (Boolean) -> Unit,
    onCheckUpdateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {

        // Pre-release switch row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPreReleaseCheckedChange(!isPreReleaseChecked) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Pre-release updates",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isPreReleaseChecked,
                onCheckedChange = onPreReleaseCheckedChange
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )

        // Check update row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckUpdateClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Check for update",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Version: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
            Text(
                "App ID: ${BuildConfig.APPLICATION_ID}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
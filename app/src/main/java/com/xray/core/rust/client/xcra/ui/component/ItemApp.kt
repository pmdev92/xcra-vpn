package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.dto.AppInfo
import com.xray.core.rust.client.xcra.extension.toImageBitmap


@Composable
fun ItemApp(
    appInfo: AppInfo,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent
            )
            .clickable(
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            bitmap = appInfo.appIcon.toImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = appInfo.appName,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = appInfo.packageName,
                maxLines = 3,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Checkbox(
            checked = appInfo.isSelected,
            onCheckedChange = null,
            modifier = Modifier.padding(8.dp)
        )
    }
}
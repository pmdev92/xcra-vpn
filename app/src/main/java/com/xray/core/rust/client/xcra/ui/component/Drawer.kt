package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.enums.DrawerItem

@Composable
fun AppDrawer(
    version: String = "1.0.0",
    onItemClick: (DrawerItem) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(280.dp)
            .windowInsetsPadding(
                WindowInsets.systemBars
            ),
        drawerShape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.height(46.dp)
            )

            DrawerGroup(
                items = mainGroup,
                onItemClick = onItemClick
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            DrawerGroup(
                items = secondaryGroup,
                onItemClick = onItemClick
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DrawerGroup(
    items: List<DrawerMenuItem>,
    onItemClick: (DrawerItem) -> Unit
) {
    items.forEach { item ->
        NavigationDrawerItem(
            shape = RectangleShape,
            label = { Text(item.title) },
            icon = {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title
                )
            },
            selected = false,
            onClick = { onItemClick(item.item) }
        )
    }
}


private data class DrawerMenuItem(
    val item: DrawerItem,
    val title: String,
    val icon: ImageVector
)

private val mainGroup = listOf(
    DrawerMenuItem(
        DrawerItem.GroupSettings,
        "Subscription settings",
        Icons.Outlined.Subscriptions
    ),
    DrawerMenuItem(
        DrawerItem.PerAppProxy,
        "Per-app proxy",
        Icons.Outlined.Apps
    ),
    DrawerMenuItem(
        DrawerItem.Routing,
        "Routing",
        Icons.Outlined.Route
    ),
    DrawerMenuItem(
        DrawerItem.UserAsset,
        "User assets",
        Icons.Outlined.Folder
    ),
    DrawerMenuItem(
        DrawerItem.Settings,
        "Settings",
        Icons.Outlined.Settings
    )
)

private val secondaryGroup = listOf(
    DrawerMenuItem(
        DrawerItem.Logcat,
        "Logcat",
        Icons.Outlined.Terminal
    ),
    DrawerMenuItem(
        DrawerItem.CheckForUpdate,
        "Check for update",
        Icons.Outlined.SystemUpdate
    ),
    DrawerMenuItem(
        DrawerItem.About,
        "About",
        Icons.Outlined.Info
    )
)
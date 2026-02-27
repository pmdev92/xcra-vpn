package com.xray.core.rust.client.xcra.ui.component


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.dto.GroupItem
import com.xray.core.rust.client.xcra.ui.model.MainViewModelAccessor

data class GroupUiItem(
    val uuid: String,
    val groupItem: GroupItem
)

enum class ItemAction {
    Share,
    Edit,
    Remove,
}


@Composable
fun GroupTab() {
    val model = MainViewModelAccessor.mainViewModel
    val selectedTab = model.selectedGroupIndex

    PrimaryScrollableTabRow(
        edgePadding = 0.dp,
        selectedTabIndex = selectedTab.value
    ) {
        model.groups.forEachIndexed { index, group ->
            Tab(
                selected = selectedTab.value == index,
                onClick = {
                    model.setSelectedGroup(group.uuid)
                },
                text = { Text(group.groupItem.remarks) }
            )
        }
    }
}


@Composable
fun GroupItem(
    groupUiItem: GroupUiItem,
    onActionClick: (ItemAction) -> Unit,
    onActionToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = groupUiItem.groupItem.remarks,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (groupUiItem.groupItem.url.isNotEmpty()) {
                    IconButton(onClick = { onActionClick(ItemAction.Share) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
                IconButton(onClick = { onActionClick(ItemAction.Edit) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = { onActionClick(ItemAction.Remove) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
                if (groupUiItem.groupItem.url.isNotEmpty()) {
                    Switch(
                        checked = groupUiItem.groupItem.enabled,
                        onCheckedChange = { checked ->
                            onActionToggle(checked)
                        },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
        }
        if (groupUiItem.groupItem.url.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupUiItem.groupItem.url,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
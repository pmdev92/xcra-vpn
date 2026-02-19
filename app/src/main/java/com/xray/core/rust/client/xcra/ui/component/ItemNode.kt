package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.dto.NodeInfo
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.dto.pingColor
import com.xray.core.rust.client.xcra.dto.pingText
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.ui.model.MainViewModelAccessor
import com.xray.core.rust.client.xcra.ui.theme.XcraTheme
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme

data class NodeUiItem(
    val uuid: String,
    val nodeItem: NodeItem,
    val nodeInfo: MutableState<NodeInfo?> = mutableStateOf(null)
)

@Composable
fun ItemNodeCard(
    item: NodeUiItem,
    isFirst: Boolean,
    isLast: Boolean,
    onShare: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {

    val model = MainViewModelAccessor.mainViewModel
    val top = if (isFirst) {
        12.dp
    } else {
        6.dp
    }
    val bottom = if (isLast) {
        12.dp
    } else {
        6.dp
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = top, bottom = bottom, start = 8.dp, end = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    model.setSelectedNode(item.uuid)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                val indicatorColor = if (item.uuid == model.selectedNodeUuid.value) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    Color.Transparent
                }

                // Indicator
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(indicatorColor)

                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(
                            8.dp
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Main info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(
                                text = item.nodeItem.remarks,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val subRemarks = item.nodeItem.subscriptionRemarks
                                if (!subRemarks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.nodeItem.subscriptionRemarks,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Text(
                                    text = item.nodeItem.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Action buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onShare() }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share"
                                )
                            }
                            IconButton(onClick = { onEdit() }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                            IconButton(onClick = { onDelete() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Type & test result
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.nodeItem.configType.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = XcraTheme.colorScheme.configType,
                            modifier = Modifier.weight(1f)
                        )
                        val ping: String = item.nodeInfo.value?.pingText() ?: ""
                        val color: Color = item.nodeInfo.value?.pingColor() ?: Color.Transparent
                        Text(
                            text = ping,
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun Preview() {
    XcraVPNTheme(darkTheme = true) {
        Box(
        ) {
            val item = NodeUiItem("10", NodeItem.create(ConfigType.VLESS))
            ItemNodeCard(
                isFirst = false,
                isLast = false,
                item = item
            )
        }
    }
}
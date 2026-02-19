package com.xray.core.rust.client.xcra.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.PingState
import com.xray.core.rust.client.xcra.enums.VpnState
import com.xray.core.rust.client.xcra.ui.component.GroupTab
import com.xray.core.rust.client.xcra.ui.component.ItemNodeCard
import com.xray.core.rust.client.xcra.ui.model.MainViewModelAccessor
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val model = MainViewModelAccessor.mainViewModel
    val items = model.nodes
    val isLoading by model.isLoading
    val focusManager = LocalFocusManager.current
    val baseHeight = 56


    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GroupTab()
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val hapticFeedback = LocalHapticFeedback.current
            val lazyListState = rememberLazyListState()
            val reorderableLazyListState =
                rememberReorderableLazyListState(lazyListState) { from, to ->
                    val aaa = items.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                    model.updateNodes(aaa)
                }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                state = lazyListState,
                contentPadding = PaddingValues(
                    bottom = (2 * baseHeight).dp
                )
            ) {
                itemsIndexed(items, key = { _, item -> item.uuid }) { index, item ->
                    ReorderableItem(reorderableLazyListState, key = item.uuid) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation.value)
                                .draggableHandle(
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    },
                                    dragGestureDetector = DragGestureDetector.LongPress
                                )
                        ) {
                            ItemNodeCard(
                                isFirst = index == 0,
                                isLast = index == items.lastIndex,
                                item = item,
                                onDelete = {
                                    model.deleteNode(context, item.uuid)
                                },
                                onEdit = {
                                    model.editNode(item.uuid)
                                },
                                onShare = {
                                    model.shareNode(item.uuid)
                                }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            if (items.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nodes is empty",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height((2 * baseHeight).dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((1.5 * baseHeight).dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((.5 * baseHeight).dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(baseHeight.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    model.testActiveNode()
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            when (model.vpnState) {
                                VpnState.DISCONNECTED -> {
                                    Text(stringResource(R.string.state_disconnected))
                                }

                                VpnState.DISCONNECTING -> {
                                    Text(stringResource(R.string.state_disconnecting))
                                }

                                VpnState.CONNECTING -> {
                                    Text(stringResource(R.string.state_connecting))
                                }

                                else -> {
                                    when (model.nodePingState) {
                                        PingState.NOT_MEASURED -> {
                                            Text(stringResource(R.string.state_connected_1))
                                        }

                                        PingState.MEASURING -> {
                                            Text(stringResource(R.string.state_connected_2))
                                        }

                                        PingState.MEASURED_SUCCESS -> {
                                            Text(
                                                stringResource(
                                                    R.string.state_connected_3,
                                                    model.nodePingValue
                                                )
                                            )
                                        }

                                        PingState.MEASURED_TIMEOUT -> {
                                            Text(stringResource(R.string.state_connected_4))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(baseHeight.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        TextField(
                            value = model.filter,
                            onValueChange = {
                                model.updateFilter(it)
                            },
                            placeholder = { Text("Search...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (model.filter.isNotEmpty()) {
                                    IconButton(
                                        onClick = { model.updateFilter("") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        modifier = Modifier
                            .width(baseHeight.dp)
                            .fillMaxHeight()
                            .clip(CircleShape),
                        shape = CircleShape,
                        tonalElevation = 12.dp,
                        shadowElevation = 12.dp
                    ) {
                        when (model.vpnState) {
                            VpnState.DISCONNECTED -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.tertiary)
                                ) {
                                    IconButton(
                                        onClick = { model.toggleVpn() },
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiary
                                        )
                                    }
                                }

                            }

                            VpnState.CONNECTED -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    IconButton(
                                        onClick = { model.toggleVpn() },
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Stop,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            VpnState.CONNECTING, VpnState.DISCONNECTING -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}
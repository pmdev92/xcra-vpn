package com.xray.core.rust.client.xcra.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun XcraDividerField(
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}

@Composable
fun XcraTitleTextField(
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun XcraEditTextField(
    titleResId: Int,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    large: Boolean = false
) {
    var minLines = 1
    var maxLines = 1
    var singleLine = true
    if (large) {
        minLines = 4
        maxLines = 4
        singleLine = false
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = stringResource(id = titleResId),
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            isError = isError,
        )
    }
}

@Composable
fun XcraDropDown(
    titleResId: Int,
    items: Array<String>,
    selected: String,
    onValueChange: (String) -> Unit,
    isCapitalize: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var widthDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdown_rotation"
    )

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    widthDp = with(density) { size.width.toDp() }
                }
        ) {
            val selected = if (isCapitalize) {
                selected.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } else {
                selected
            }
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = titleResId)) },
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            )

            DropdownMenu(
                modifier = Modifier
                    .width(widthDp),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEach { entry ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onValueChange(entry)
                            expanded = false
                        },
                        text = {
                            val text = if (isCapitalize) {
                                entry.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            } else {
                                entry
                            }
                            Text(text = text)
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    expanded = !expanded
                }
        )
    }
}

@Composable
fun XcraSwitchField(
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        Switch(
            checked = value,
            onCheckedChange = { checked ->
                onValueChange(checked)
            },
            modifier = Modifier.scale(0.8f)
        )
    }
}
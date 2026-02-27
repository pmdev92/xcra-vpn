package com.xray.core.rust.client.xcra.dto

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.xray.core.rust.client.xcra.ui.theme.XcraTheme

data class NodeInfo(var result: Long = 0L) {
}

@Composable
fun NodeInfo.pingText(): String {
    if (result < 0L) {
        return "timeout"
    }
    return result.toString() + "ms"
}

@Composable
fun NodeInfo.pingColor(): Color {
    if (result < 0L) return XcraTheme.colorScheme.pingTimeout
    if (result < 1200L) return XcraTheme.colorScheme.pingGood
    if (result < 2200L) return XcraTheme.colorScheme.pingMedium
    return XcraTheme.colorScheme.pingBad
}
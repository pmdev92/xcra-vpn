package com.xray.core.rust.client.xcra.extension

import android.annotation.SuppressLint

const val THRESHOLD = 1000L
const val DIVISOR = 1024.0

/**
 * Converts a Long value to a traffic string.
 *
 * @return The traffic string.
 */
@SuppressLint("DefaultLocale")
fun Long.toTrafficString(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var size = this.toDouble()
    var unitIndex = 0
    while (size >= THRESHOLD && unitIndex < units.size - 1) {
        size /= DIVISOR
        unitIndex++
    }
    return String.format("%.1f %s", size, units[unitIndex])
}
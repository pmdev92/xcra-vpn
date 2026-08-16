package com.xray.core.rust.client.xcra.dto

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogEntry(val id: Long, val timestamp: Long, val tag: String, val message: String) {
    fun display(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return "[${sdf.format(Date(timestamp))}] $tag: $message"
    }
}

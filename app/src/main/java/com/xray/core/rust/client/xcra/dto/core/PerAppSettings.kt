package com.xray.core.rust.client.xcra.dto.core

data class PerAppSettings(
    val enable: Boolean,
    val bypass: Boolean,
    val items: List<String>
)
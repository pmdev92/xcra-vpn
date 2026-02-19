package com.xray.core.rust.client.xcra.dto

data class ConfigResult(
    var status: Boolean,
    var nodeItem: NodeItem? = null,
    var uuid: String? = null,
    var json: String = "",
    var socksPort: Int = 1080,
)


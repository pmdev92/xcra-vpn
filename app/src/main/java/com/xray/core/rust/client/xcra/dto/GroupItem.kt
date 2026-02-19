package com.xray.core.rust.client.xcra.dto

data class GroupItem(
    var addedTime: Long = System.currentTimeMillis(),
    var editTime: Long = System.currentTimeMillis(),
    var remarks: String = "",
    var url: String = "",
    var enabled: Boolean = true,
    var autoUpdate: Boolean = false,
    var allowInsecureUrl: Boolean = false,
    var lastUpdated: Long = -1,
)


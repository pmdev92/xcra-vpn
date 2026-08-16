package com.xray.core.rust.client.xcra.enums


enum class VpnState(val value: Int) {

    DISCONNECTED(0xFF1),
    CONNECTED(0xFF2),
    CONNECTING(0xFF5),
    DISCONNECTING(0xFF6);

    companion object {
        fun fromInt(value: Int) =
            VpnState.entries.firstOrNull { it.value == value }
    }
}

